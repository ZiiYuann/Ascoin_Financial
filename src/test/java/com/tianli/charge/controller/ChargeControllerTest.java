package com.tianli.charge.controller;

import com.tianli.tool.webhook.DingDingUtil;
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
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", l + "");
        System.out.println(l);
        System.out.println(sign);
//        HttpClient client = HttpClientBuilder.create().build();
//        // BSC链
//        HttpPost httpPost = new HttpPost("https://www.assureadd.com/api/charge/recharge/TRON");
////        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/TRON");
//        httpPost.setHeader("Content-Type", "text/plain");
//        httpPost.setHeader("Sign", sign);
//        httpPost.setHeader("timestamp", l + "");
//        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1749794456699984189, \"to\": \"TVvPCbvHZgA2aQvPjpnynJBsQs2VTCChb9\", \"from\": \"TJJfHtf2LYahnY5mSiS1A7faQUL9LDeBUA\", \"hash\": \"55d662baf8164da8d708a0000367bc106c4b215a02bbd4e23e7d7da4d4a30c32\", \"block\": 46049172, \"value\": \"1498200000\", \"netFee\": \"0\", \"status\": 1, \"netUsage\": 345, \"energyFee\": \"0\", \"createTime\": {\"date\": {\"day\": 18, \"year\": 2022, \"month\": 11}, \"time\": {\"hour\": 9, \"nano\": 0, \"minute\": 13, \"second\": 42}}, \"energyUsage\": 14650, \"contractAddress\": \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\", \"energyUsageTotal\": 14650, \"originEnergyUsage\": 0}], \"standardCurrency\": []}"));
//        client.execute(httpPost);
    }

    @Test
    void withdrawCallback() throws IOException {

        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", l + "");


        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/withdraw/OPTIMISTIC");
//        HttpPost httpPost = new HttpPost("https://www.assureadd.com/api/charge/withdraw/TRON");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("        {\"token\":[],\"standardCurrency\":[{\"to\":\"0x1ce95678e7a720debea6b813fceb55b92d6515c3\",\"from\":\"0x504958caa2488691d85d5b36670dc9411f8dc383\",\"hash\":\"0x91c1cee93158a6d1ed5de8886b8ebe346870af64ddbb109e0ba2985c25d76a34\",\"block\":62159125,\"value\":1100000000000000,\"createTime\":{\"date\":{\"year\":2023,\"month\":1,\"day\":8},\"time\":{\"hour\":10,\"minute\":39,\"second\":40,\"nano\":0}}}]}\n"));

        client.execute(httpPost);
    }


}