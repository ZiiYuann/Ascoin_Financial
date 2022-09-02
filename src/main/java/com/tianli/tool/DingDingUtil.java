package com.tianli.tool;

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

    public static String postWithJson(Object message) {

        JSONObject msg = new JSONObject();
        msg.putOnce("msgtype", "link");
        msg.putOnce("link", message);
        try {
            HttpPost httpPost = new HttpPost(getSign());
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setEntity(new StringEntity(msg.toString(),StandardCharsets.UTF_8));
            HttpResponse execute = client.execute(httpPost);
            return EntityUtils.toString(execute.getEntity());
        } catch (Exception e) {
            log.error("消息发送失败！");
        }
        return null;
    }

    private static String getSign() throws Exception {
        String token = "1a1216a39f18e8022b6795014424a9fcf5d62a5f00d3666c11127b21841eb718";
        String secret = "SEC52152f460aaf1c4c77592f46674aadf9592fcca6d99974b0b7fb74cd66f20be3";
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
