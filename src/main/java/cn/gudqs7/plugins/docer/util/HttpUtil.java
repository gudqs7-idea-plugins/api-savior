package cn.gudqs7.plugins.docer.util;

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
        OutputStream os = null;

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
                os = conn.getOutputStream();
                os.write(outputStr.getBytes(Consts.UTF_8));
                os.flush();
                os.close();
            }

            InputStream inputStream = conn.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[4096];

            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }

            byte[] var10 = outputStream.toByteArray();
            return new String(var10, Consts.UTF_8);
        } catch (Exception var14) {
            throw new RuntimeException("http client--> httpRequest exception: " + var14.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


}
