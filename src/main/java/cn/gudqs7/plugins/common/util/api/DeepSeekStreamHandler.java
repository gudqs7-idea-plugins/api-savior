package cn.gudqs7.plugins.common.util.api;

import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.progress.ProcessCanceledException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeepSeekStreamHandler {
    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper mapper;

    public static DeepSeekStreamHandler getInstance() {
        String aiSk = System.getenv("RUST_SAVIOR_AI_SK");
        return new DeepSeekStreamHandler(aiSk);
    }

    public DeepSeekStreamHandler(String apiKey) {
        this(apiKey, "https://api.deepseek.com/chat/completions");
    }

    public DeepSeekStreamHandler(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.mapper = new ObjectMapper();
    }

    public void streamChat(String userMessage, StreamCallback callback) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection();
            sendRequest(connection, userMessage);
            handleResponse(connection, callback);
        } catch (ProcessCanceledException ignored) {
        } catch (Throwable e) {
            ExceptionUtil.handleException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection createConnection() throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Accept", "text/event-stream");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000); // 2分钟超时

        return connection;
    }

    private void sendRequest(HttpURLConnection connection, String userMessage) throws IOException {
        String requestBody = buildRequestBody(userMessage);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private String buildRequestBody(String userMessage) {
        return String.format("""
                {
                    "model": "deepseek-chat",
                    "messages": [
                        
                        {
                            "role": "system",
                             "content": "你是一个专业的给rust函数写中文文档的专家,只返回注释 不用```包裹注释"
                        },
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ],
                    "stream": true
                }
                """, escapeJsonString(userMessage));
    }

    private String escapeJsonString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void handleResponse(HttpURLConnection connection, StreamCallback callback) throws IOException {
        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorMessage = readErrorStream(connection);
            callback.onError(new RuntimeException("AI请求失败, HTTP状态码: " + responseCode + "; 错误信息: " + errorMessage));
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();

                    if ("[DONE]".equals(data)) {
                        callback.onComplete();
                        break;
                    }

                    if (!data.isEmpty()) {
                        processDataChunk(data, callback);
                    }
                }
            }
        }
    }

    private void processDataChunk(String jsonData, StreamCallback callback) {
        try {
            JsonNode root = mapper.readTree(jsonData);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                JsonNode choice = choices.get(0);
                JsonNode delta = choice.path("delta");

                if (delta.has("content")) {
                    String content = delta.path("content").asText();
                    if (!content.isEmpty()) {
                        callback.onContent(content);
                    }
                }

                if (choice.has("finish_reason")) {
                    String finishReason = choice.path("finish_reason").asText();
                    if (!"null".equals(finishReason) && !finishReason.isEmpty()) {
                        callback.onFinish(finishReason);
                    }
                }
            }

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private String readErrorStream(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {

            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            return error.toString();

        } catch (IOException e) {
            return "无法读取错误信息: " + e.getMessage();
        }
    }

    public interface StreamCallback {
        void onContent(String content);

        void onFinish(String reason);

        void onComplete();

        void onError(Exception e);
    }
}