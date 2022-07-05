package com.tianli.tool.http;

import com.google.gson.Gson;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    public enum Method {
        GET, POST, PUT
    }

    /**
     * 待请求的url
     */
    private String url = null;

    /**
     * 默认的请求方式
     */
    private Method method = Method.GET;

    /**
     * Get方式请求时对应的参数
     */
    private String queryString = null;

    private Map<String, String> queryMap = null;

    private byte[] binary = null;
    private InputStream stream = null;
    private File file = null;
    private ContentType contentType = null;

    /**
     * 默认的请求编码方式
     */
    private Charset charset = StandardCharsets.UTF_8;

    private Map<String, String> requestHeader = new HashMap<>();

    public HttpRequest() {
//        requestHeader.put("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
        requestHeader.put("User-Agent", "Mozilla/4.0");
    }

    public String getQueryString() {
        return queryString;
    }

    public HttpRequest setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public HttpRequest setJsonString(String queryString) {
        this.queryString = queryString;
        this.contentType = ContentType.APPLICATION_JSON.withCharset(charset);
        return this;
    }

    public HttpRequest setJsonObject(Object o) {
        this.queryString = new Gson().toJson(o);
        this.contentType = ContentType.APPLICATION_JSON.withCharset(charset);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public HttpRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public HttpRequest setMethod(Method method) {
        this.method = method;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public HttpRequest setQueryMap(Map<String, String> queryMap) {
        this.queryMap = queryMap;
        return this;
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    public HttpRequest setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
        return this;
    }

    public byte[] getBinary() {
        return binary;
    }

    public HttpRequest setBinary(byte[] binary) {
        this.binary = binary;
        return this;
    }

    public InputStream getStream() {
        return stream;
    }

    public HttpRequest setStream(InputStream stream) {
        this.stream = stream;
        return this;
    }

    public File getFile() {
        return file;
    }

    public HttpRequest setFile(File file) {
        this.file = file;
        return this;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public HttpRequest setContentType(ContentType contentType) {
        this.contentType = contentType.withCharset(charset);
        return this;
    }

    public HttpRequest setContentType(String contentType) {
        this.contentType = ContentType.create(contentType, charset);
        return this;
    }
}
