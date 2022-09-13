package com.tianli.charge.controller;

import com.tianli.tool.DingDingUtil;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.time.TimeTool;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.crypto.util.DigestFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

class ChargeControllerTest {

    @Test
    void rechargeCallback() throws IOException {
        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", l + "");

        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
//        HttpPost httpPost = new HttpPost("https://www.assureadd.com/api/charge/recharge/BSC");
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/BSC");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
//        httpPost.setEntity(new StringEntity(""));
        httpPost.setEntity(new StringEntity("{\"token\":[{\"id\":1742812302590244600,\"to\":\"0x22a646ef282b62af0391eba132c53dc208928182\",\"from\":\"0x504958caa2488691d85d5b36670dc9411f8dc383\",\"hash\":\"0xfe8f5b8ad07a3d21f11a7824527216b66b1db2b6e3819d7ec72154de0c1850da\",\"block\":21290931,\"value\":3000000000000000000,\"createTime\":{\"date\":{\"day\":2,\"year\":2022,\"month\":9},\"time\":{\"hour\":7,\"nano\":0,\"minute\":35,\"second\":21}},\"contractAddress\":\"0x55d398326f99059ff775485246999027b3197955\"}],\"standardCurrency\":[]}"));
        client.execute(httpPost);
    }

    @Test
    void withdrawCallback() throws IOException {

        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", l + "");


        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/withdraw/TRON");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("{\"token\":[{\"id\":1740027775524498000,\"to\":\"TD4FCCpMEEDNX41Lo8xf6SqUomqaAPHJEe\",\"from\":\"TKTisiaH4CMTAWAddRmwTJLdaoHKHHTeRj\",\"hash\":\"c7ca172b8816aab174d4ace7b9c6065e0ded71bb2fa996d6cc0f23d9c1981b6e\",\"status\":1,\"block\":42948067,\"value\":1000000,\"createTime\":{\"date\":{\"day\":26,\"year\":2022,\"month\":7},\"time\":{\"hour\":13,\"nano\":0,\"minute\":52,\"second\":18}},\"contractAddress\":\"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\"}],\"standardCurrency\":[]}"));
        client.execute(httpPost);
    }

    @Test
    void dingdingTest() {
        String a = DingDingUtil.postWithJson("测试发送数据");
        System.out.println(a);
    }


}