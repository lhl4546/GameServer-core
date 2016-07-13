package core.fire.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * 基于Apache httpclient4实现的GET\POST方法
 * 
 * @author lhl
 *
 *         2016年5月25日 下午2:36:42
 */
public class ApacheHttpUtil
{
    private static final int BUFFER_SIZE = 1024;
    private static HttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager();

    /**
     * 发送HTTP GET请求，并返回字符串应答
     * 
     * @param url
     * @param params
     * @return
     */
    public static String GET(String url, Iterable<NameValuePair> params) {
        RequestBuilder requestBuilder = RequestBuilder.get(url);
        for (NameValuePair pair : params) {
            requestBuilder.addParameter(pair);
        }
        return execute(requestBuilder.build());
    }

    /**
     * 发送HTTP POST请求，并返回字符串应答
     * <p>
     * MIME: application/x-www-form-urlencoded
     * 
     * @param url
     * @param params
     * @param return
     */
    public static String POST(String url, Iterable<NameValuePair> params) {
        HttpUriRequest request = RequestBuilder.post(url).setEntity(new UrlEncodedFormEntity(params)).build();
        return execute(request);
    }

    /**
     * 发送HTTP POST请求，并返回字符串应答
     * <p>
     * MIME: text/plain
     * 
     * @param url
     * @param data
     * @return
     */
    public static String POST(String url, String data) {
        HttpUriRequest request = RequestBuilder.post(url).setEntity(new StringEntity(data, StandardCharsets.UTF_8)).build();
        return execute(request);
    }

    private static String execute(HttpUriRequest request) {
        CloseableHttpClient client = newHttpClient();
        try {
            CloseableHttpResponse rsp = client.execute(request);
            HttpEntity entity = rsp.getEntity();

            try (InputStream inStream = entity.getContent()) {
                return copyToString(inStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CloseableHttpClient newHttpClient() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(10000).setConnectionRequestTimeout(10000).build();
        return HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(config).build();
    }

    /**
     * 将输入流内容转换为字符串
     * 
     * @param in
     * @param charset
     * @return
     * @throws IOException
     */
    private static String copyToString(InputStream in, Charset charset) throws IOException {
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }
}
