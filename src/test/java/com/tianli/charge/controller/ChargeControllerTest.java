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
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", l + "");

        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
        HttpPost httpPost = new HttpPost("https://www.assureadd.com/api/charge/recharge/TRON");
//        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/TRON");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
//        httpPost.setEntity(new StringEntity(""));
        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1749794456699984189, \"to\": \"TVvPCbvHZgA2aQvPjpnynJBsQs2VTCChb9\", \"from\": \"TJJfHtf2LYahnY5mSiS1A7faQUL9LDeBUA\", \"hash\": \"55d662baf8164da8d708a0000367bc106c4b215a02bbd4e23e7d7da4d4a30c32\", \"block\": 46049172, \"value\": \"1498200000\", \"netFee\": \"0\", \"status\": 1, \"netUsage\": 345, \"energyFee\": \"0\", \"createTime\": {\"date\": {\"day\": 18, \"year\": 2022, \"month\": 11}, \"time\": {\"hour\": 9, \"nano\": 0, \"minute\": 13, \"second\": 42}}, \"energyUsage\": 14650, \"contractAddress\": \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\", \"energyUsageTotal\": 14650, \"originEnergyUsage\": 0}], \"standardCurrency\": []}"));
        client.execute(httpPost);
    }

    @Test
    void withdrawCallback() throws IOException {

        long l = TimeTool.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", l + "");


        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/withdraw/BSC");
//        HttpPost httpPost = new HttpPost("https://www.assureadd.com/api/charge/withdraw/TRON");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("        {\"token\": [{\"id\": 1749804255826008264, \"to\": \"0x9813ac2199ac88f4151fed8d4c5083c7118b7b6e\", \"from\": \"0x4098b66ff83bf99bc9c81a68be7b045c79ebbb97\", \"hash\": \"0x21f3e4fa124b7496babc002e5a09c1ff7c059f2b949ca3629e7c24cf04a4cc51\", \"block\": 23151332, \"value\": \"100000000000000000\", \"createTime\": {\"date\": {\"day\": 18, \"year\": 2022, \"month\": 11}, \"time\": {\"hour\": 11, \"nano\": 0, \"minute\": 49, \"second\": 25}}, \"contractAddress\": \"0x55d398326f99059ff775485246999027b3197955\"}], \"standardCurrency\": []}\n"));

        client.execute(httpPost);
    }


}