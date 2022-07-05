package com.tianli.tool.http;

import com.tianli.common.log.LoggerHandle;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.ApplicationContextTool;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wangqiyun on 2017/6/3.
 */
public class HttpHandler {

    public static final int maxTotal = 200;
    public static final int maxPerRoute = 50;
    public static final int socketTimeout = 10000;
    public static final int connectTimeout = 10000;
    public static final int connectionRequestTimeout = 10000;


    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
    private static RequestConfig requestConfig;

    static {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                .getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
                .getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf)
                .register("https", sslsf).build();
        poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);

        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                connectTimeout).build();
    }

    private static final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager).setDefaultRequestConfig(requestConfig).build();

    public static final ThreadLocal<CloseableHttpClient> HTTP_CLIENT_THREAD_LOCAL = new ThreadLocal<>();

    public static CloseableHttpClient getHttpClient() {
        CloseableHttpClient closeableHttpClient = HTTP_CLIENT_THREAD_LOCAL.get();
        if (closeableHttpClient == null) closeableHttpClient = httpClient;
        return closeableHttpClient;
    }

    public static HttpResponse execute(HttpRequest request) {
        try {
            Charset charset = request.getCharset();
            HttpRequestBase httpUriRequest;
            if (HttpRequest.Method.GET.equals(request.getMethod())) {
                URI uri;
                if (request.getQueryMap() != null && !request.getQueryMap().isEmpty()) {
                    List<NameValuePair> list = new ArrayList<>();
                    for (Map.Entry<String, String> entry : request.getQueryMap().entrySet()) {
                        if (entry.getValue() != null)
                            list.add(new NameValuePairImp(entry.getKey(), entry.getValue()));
                    }
                    uri = new URIBuilder(new URI(request.getUrl())).setCharset(charset).addParameters(list).build();
                } else if (!StringUtils.isEmpty(request.getQueryString())) {
                    uri = new URI(request.getUrl() + "?" + request.getQueryString());
                } else
                    uri = new URI(request.getUrl());
                HttpGet httpGet = new HttpGet(uri);
                for (Map.Entry<String, String> entry : request.getRequestHeader().entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
                httpUriRequest = httpGet;

            } else if (HttpRequest.Method.POST.equals(request.getMethod())) {
                URI uri = new URI(request.getUrl());
                HttpPost httpPost = new HttpPost(uri);
                if (request.getQueryMap() != null && !request.getQueryMap().isEmpty()) {
                    List<NameValuePair> list = new ArrayList<>();
                    for (Map.Entry<String, String> entry : request.getQueryMap().entrySet()) {
                        if (entry.getValue() != null)
                            list.add(new NameValuePairImp(entry.getKey(), entry.getValue()));
                    }
                    httpPost.setEntity(new UrlEncodedFormEntity(list, charset));
                } else {
                    boolean no_content = false;
                    EntityBuilder entityBuilder = EntityBuilder.create();
                    if (!StringUtils.isEmpty(request.getQueryString())) {
                        entityBuilder.setText(request.getQueryString());
                        entityBuilder.setContentType(ContentType.create("text/plain", charset));
                    } else if (request.getBinary() != null)
                        entityBuilder.setBinary(request.getBinary());
                    else if (request.getStream() != null)
                        entityBuilder.setStream(request.getStream());
                    else if (request.getFile() != null)
                        entityBuilder.setFile(request.getFile());
                    else {
                        no_content = true;
                    }

                    if (!no_content) {
                        if (request.getContentType() != null)
                            entityBuilder.setContentType(request.getContentType().withCharset(charset));
                        httpPost.setEntity(entityBuilder.build());
                    }
                }
                for (Map.Entry<String, String> entry : request.getRequestHeader().entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
                httpUriRequest = httpPost;
            } else if (HttpRequest.Method.PUT.equals(request.getMethod())) {
                URI uri = new URI(request.getUrl());
                HttpPut httpPut = new HttpPut(uri);
                if (request.getQueryMap() != null && !request.getQueryMap().isEmpty()) {
                    List<NameValuePair> list = new ArrayList<>();
                    for (Map.Entry<String, String> entry : request.getQueryMap().entrySet()) {
                        if (entry.getValue() != null)
                            list.add(new NameValuePairImp(entry.getKey(), entry.getValue()));
                    }
                    httpPut.setEntity(new UrlEncodedFormEntity(list, charset));
                } else {
                    EntityBuilder entityBuilder = EntityBuilder.create();
                    if (!StringUtils.isEmpty(request.getQueryString())) {
                        entityBuilder.setText(request.getQueryString());
                        entityBuilder.setContentType(ContentType.create("text/plain", charset));
                    } else if (request.getBinary() != null)
                        entityBuilder.setBinary(request.getBinary());
                    else if (request.getStream() != null)
                        entityBuilder.setStream(request.getStream());
                    else if (request.getFile() != null)
                        entityBuilder.setFile(request.getFile());
                    if (request.getContentType() != null)
                        entityBuilder.setContentType(request.getContentType().withCharset(charset));
                    httpPut.setEntity(entityBuilder.build());
                }
                for (Map.Entry<String, String> entry : request.getRequestHeader().entrySet()) {
                    httpPut.addHeader(entry.getKey(), entry.getValue());
                }
                httpUriRequest = httpPut;
            } else throw new HttpException();

            Environment environment = ApplicationContextTool.getBean(Environment.class);
            if (environment != null) {
                String external_url = environment.getProperty("EXTERNAL_URL");
                if (!StringUtils.isEmpty(external_url)) {
                    URI uri = httpUriRequest.getURI();
                    httpUriRequest.addHeader("HOSTS", uri.getScheme() + "://" + uri.getAuthority());
                    httpUriRequest.setURI(new URI(external_url + uri.getRawPath() + (uri.getRawQuery() == null ? "" : ("?" + uri.getRawQuery()))));
                }
            }

            HttpResponse httpResponse = new HttpResponse();
            CloseableHttpResponse response = null;
            LoggerHandle loggerHandle = ApplicationContextTool.getBean(LoggerHandle.class);
            try {
                if (loggerHandle != null)
//                    loggerHandle.log(MapTool.Map().put("note", "httpRequest").put("body", request));
                response = getHttpClient().execute(httpUriRequest);
                if (response == null) throw new HttpException();
                httpResponse.setResponseHeaders(response.getAllHeaders());
                HttpEntity entity = response.getEntity();
                httpResponse.setByteResult(EntityUtils.toByteArray(entity));
                EntityUtils.consumeQuietly(entity);
                httpResponse.setResponseHeaders(response.getAllHeaders());
            } finally {
                if (response != null)
                    response.close();
            }
            if (loggerHandle != null) {
                String stringResult = httpResponse.getStringResult();
                if(stringResult != null && stringResult.length() > 300){
                    stringResult = "";
                }
//                loggerHandle.log(MapTool.Map().put("note", "httpResponse").put("body", stringResult));
            }
            return httpResponse;
        } catch (Exception e) {
            throw ErrorCodeEnum.NETWORK_ERROR.generalException(e);
        }
    }
}
