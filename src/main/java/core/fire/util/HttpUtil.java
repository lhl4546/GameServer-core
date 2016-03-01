/**
 * 
 */
package core.fire.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * @author lhl
 *
 *         2015年12月30日 下午2:41:08
 */
final public class HttpUtil
{
    private HttpUtil() {
    }

    private static final int HTTP_TIMEOUT_MS = 1000 * 20; // 连接或者读取超时

    /**
     * 发送HTTP GET请求
     * 
     * @param getUrl 请求url，附带get参数
     * @return 应答消息
     * @throws Exception
     */
    public static String GET(String getUrl) throws Exception {
        Objects.requireNonNull(getUrl, "GET: getUrl 不能为空");

        URL url = new URL(getUrl);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.connect();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    /**
     * 发送HTTP POST请求，以(application/x-www-form-urlencoded)形式发送数据
     * 
     * @param postUrl 请求url
     * @param param post数据
     * @return 应答，waitForReturn为false时直接返回null
     * @throws Exception
     */
    public static String POST(String postUrl, byte[] param) throws Exception {
        Objects.requireNonNull(postUrl, "POST: postUrl 不能为空");
        Objects.requireNonNull(param, "POST: param 不能为空");

        URL url = new URL(postUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(param.length));
        try (OutputStream out = conn.getOutputStream()) {
            out.write(param);
            out.flush();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }
}
