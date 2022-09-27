package com.tianli.tool.webhook;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-02
 **/
@Slf4j
public class DingDingUtil {

    private final static HttpClient client;


    static {
        client = HttpClients.createDefault();
    }

    public static String linkType(Object message,String token,String secret) {

        JSONObject msg = new JSONObject();
        msg.putOnce("msgtype", "link");
        msg.putOnce("link", message);
        try {
            HttpPost httpPost = new HttpPost(getSign(token,secret));
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setEntity(new StringEntity(msg.toString(), StandardCharsets.UTF_8));
            HttpResponse execute = client.execute(httpPost);
            return EntityUtils.toString(execute.getEntity());
        } catch (Exception e) {
            log.error("消息发送失败！");
        }
        return null;
    }

    public static String textType(Object message,String token,String secret) {
        JSONObject msg = new JSONObject();
        msg.putOnce("msgtype", "text");
        JSONObject content = new JSONObject();
        content.set("content", message);
        msg.putOnce("text", content);
        try {
            HttpPost httpPost = new HttpPost(getSign(token,secret));
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setEntity(new StringEntity(msg.toString(), StandardCharsets.UTF_8));
            HttpResponse execute = client.execute(httpPost);
            return EntityUtils.toString(execute.getEntity());
        } catch (Exception e) {
            log.error("消息发送失败！");
        }
        return null;
    }

    private static String getSign(String token,String secret) throws Exception {
        String baseUrl = "https://oapi.dingtalk.com/robot/send?access_token=";
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return baseUrl + token + "&timestamp=" + timestamp + "&sign=" +
                URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8);
    }


}
