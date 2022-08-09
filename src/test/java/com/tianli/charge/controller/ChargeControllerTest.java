package com.tianli.charge.controller;

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
        HttpPost httpPost = new HttpPost("https://www.assureadd.com//api/charge/recharge/ETH");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1740648242323999537, \"to\": \"0xfafa1a6f307671e6649ad4d9258b4f85e6d1120c\", \"from\": \"0x74398c5a3f89341e5ab6d495945c0e183424e421\", \"hash\": \"0xbe0cd07d5a79a7302aecf7d3c1ccd823770e19f7fb0967d0e23e0e442077d490\", \"block\": 15305288, \"value\": 4000000, \"createTime\": {\"date\": {\"day\": 9, \"year\": 2022, \"month\": 8}, \"time\": {\"hour\": 10, \"nano\": 0, \"minute\": 17, \"second\": 35}}, \"contractAddress\": \"0xdac17f958d2ee523a2206206994597c13d831ec7\"}], \"standardCurrency\": []}"));
//        httpPost.setEntity(new StringEntity(""));
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


}