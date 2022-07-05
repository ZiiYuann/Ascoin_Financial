package com.tianli.tool.http;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.KeyVal;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author chensong
 * @date 2021-04-16 16:13
 * @since 1.0.0
 */
public class JSoupUtils {
    private static final Logger log = LoggerFactory.getLogger(JSoupUtils.class);

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
    /**
     * 五分钟超时
     */
    private static final int CONN_TIME_OUT = 1000 * 60 * 5;

    /**
     * 访问特定连接
     */
    private static Document access(Connection connection, Connection.Method method) throws IOException {
        Document document;
        if (connection == null) {
            log.error("Access failed.\nError:Connection is null");
            return null;
        }

        connection.ignoreContentType(true);
        String url = connection.request().url().toString();
        int idx = url.indexOf("?");
        String mainUlr = url;
        String host = "";
        if (idx > -1) {
            mainUlr = url.substring(0, idx);
        }
        host = mainUlr.substring(mainUlr.indexOf("//") + 2);
        host = host.substring(0, host.indexOf("/"));
        Collection<KeyVal> dataBak = new ArrayList<KeyVal>();

        //执行完execute，request会丢失。备份原collection的data
        for (KeyVal kv : connection.request().data()) {
            if (StringUtils.isNotBlank(kv.value())) {
                dataBak.add(HttpConnection.KeyVal.create(kv.key(), kv.value()));
            }
        }
        try {
            long start = System.currentTimeMillis();
            // connection//.timeout(DEFAULT_TIMEOUT)
            Connection.Response response = connection
                    .header("trad-client-name",
                            System.getProperty("project.name") == null ? "hdcs" : System.getProperty("project.name"))
//                    .header("trad-proxy-socket-timeout",DEFAULT_TIMEOUT+"")
                    .header("Cache-Control", "no-cache")
//                    .header("cookie", "_ga=GA1.2.1023112534.1618556308; ASP.NET_SessionId=ayjgr3n2za3t44bwxzgn0ujm; _gid=GA1.2.1670795441.1621235181; cf_chl_prog=a10; cf_clearance=264491ca54a272480e82f4d87294590cfee477f5-1621320619-0-250; __cflb=02DiuFnsSsHWYH8WqVXcJWaecAw5gpnmdvQUaLezm9a9r; __cf_bm=708d63b32b128dc2f986e58bed3cdceb7bee8f28-1621321733-1800-AVUcyS4+JxuTrqTv5+lbilylkqtuFsenFJ13Dh61jEq9WgASU2wNo0fUVy+IUAI5ClWAs1BLlQHC4cyb+LK6hL3+BcX3vKqZfdD2ke2j0d7I5BjjGSoVgXpF20nM8Aecrw==")
//                    .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
                    .method(method)
                    .execute();
            document = response.parse();
        } catch (IOException e) {
            log.error("Access failed.url="+url+",data="+dataBak,e);
            throw new IOException(e);
        }
        return document;
    }

    /**
     * post 请求
     */
    public static Document post(Connection connection) throws IOException {
        return access(connection, Connection.Method.POST);
    }

    /**
     * get请求
     */
    public static Document get(Connection connection) throws IOException {
        return access(connection, Connection.Method.GET);
    }

    /**
     * 得到一个标准的Jsoup Connection
     * 设置了超时时间和user_agent
     */
    public static Connection getConnection(String url) {
        return Jsoup.connect(url).followRedirects(true).timeout(CONN_TIME_OUT).userAgent(USER_AGENT);
    }

    /**
     * get方式获取响应内容
     *
     * @param url
     * @return
     */
    public static Document get(String url) throws Exception {
        Collection<KeyVal> data = Collections.emptyList();
        return get(url, data);
    }

    /**
     * get方式获取响应内容
     *
     * @param url
     * @param data
     * @return
     */
    public static Document get(String url, Collection<KeyVal> data) throws Exception {
        return get(url, data, null, null);
    }

    /**
     * get方式获取响应内容
     *
     * @param url
     * @param refer
     * @return
     */
    public static Document get(String url, String refer, String useProxy) throws Exception {
        return get(url, Collections.EMPTY_LIST, refer, useProxy);
    }

    /**
     * get方式获取响应内容
     *
     * @param url
     * @param refer
     * @return
     */
    public static Document get(String url, String refer) throws Exception {

        return get(url, refer, null);
    }

    /**
     * get方式获取响应内容
     *
     * @param url
     * @param data
     * @return
     */
    public static Document get(String url, Collection<KeyVal> data, String refer, String proxyUrl) throws Exception {
        return get(url, null, data, refer, proxyUrl);
    }

    /**
     * get方式获取响应内容
     *
     * @param url
     * @param data
     * @return
     */
    public static Document get(String url, Map<String, String> cookies, Collection<KeyVal> data, String refer,
                               String proxyUrl) throws Exception {
        long start = System.currentTimeMillis();

        try {
            Connection connection = getConnection(url).data(data);
            if (!CollectionUtils.isEmpty(cookies)) {
                connection.cookies(cookies);
            }
            if (StringUtils.isNotBlank(refer)) {
                connection.referrer(refer);
            }
            if (StringUtils.isNotBlank(proxyUrl)) {
                System.setProperty("http.proxyHost", proxyUrl.split(":")[0].trim());
                System.setProperty("http.proxyPort", proxyUrl.split(":")[1].trim());
            }
            return get(connection);
        } catch (Exception e) {
            throw e;
        } finally {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
        }
    }

    /**
     * post方式获取响应内容
     *
     * @param url
     * @return
     */
    public static Document post(String url) throws IOException {
        Collection<KeyVal> data = Collections.emptyList();
        return post(url, data);
    }

    /**
     * post方式获取响应内容
     *
     * @param url
     * @param data
     * @return
     */
    public static Document post(String url, Collection<KeyVal> data) throws IOException {
        Connection connection = getConnection(url).data(data);

        return post(connection);
    }

    /**
     * 获取响应内容
     *
     * @param url
     * @param data
     * @return
     */
    public static String getResponseText(String url, Collection<KeyVal> data) {
        Connection connection = getConnection(url).data(data);

        String responseText = null;
        try {
            responseText = post(connection).body().ownText();
        } catch (IOException e) {
            log.error("Get json failed.URL:" + url);
        }
        return responseText;
    }
}
