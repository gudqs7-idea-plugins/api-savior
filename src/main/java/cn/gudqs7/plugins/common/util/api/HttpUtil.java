package cn.gudqs7.plugins.common.util.api;

import org.apache.http.Consts;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author wq
 * @date 2021/9/21
 */
public class HttpUtil {

    public static String sendHttpWithBody(String requestUrl, String method, String outputStr, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    conn.setRequestProperty(key, value);
                }
            }
            conn.setRequestMethod(method);
            conn.setConnectTimeout(20000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.connect();
            if (null != outputStr) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(outputStr.getBytes(Consts.UTF_8));
                    os.flush();
                }
            }

            InputStream inputStream = conn.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[409600];

            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }

            //noinspection StringOperationCanBeSimplified
            return new String(outputStream.toByteArray(), Consts.UTF_8);
        } catch (Exception var14) {
            throw new RuntimeException("请求接口异常，错误信息：" + var14.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


}
