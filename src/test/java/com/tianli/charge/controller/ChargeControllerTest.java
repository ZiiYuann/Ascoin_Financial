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
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/BSC");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("{\"token\":[{\"id\":1740014641146449200,\"to\":\"TFjwm9QE6z9LTRAsVqXqwAH1wz1VLML1ox\",\"from\":\"TXULK321L5UPNkdujs6xvnHsgiFVcPMBXL\",\"hash\":\"623748f785e17935b044cd8c6a7e3827cdbe4d1f0f65d5a3ac5b462967fdea07\",\"block\":42943892,\"value\":2000000,\"status\":1,\"createTime\":{\"date\":{\"day\":26,\"year\":2022,\"month\":7},\"time\":{\"hour\":13,\"nano\":0,\"minute\":52,\"second\":18}},\"contractAddress\":\"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\"}],\"standardCurrency\":[]}"));
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
        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1739673149627121337, \"to\": \"TD4FCCpMEEDNX41Lo8xf6SqUomqaAPHJEe\", \"from\": \"TKTisiaH4CMTAWAddRmwTJLdaoHKHHTeRj\", \"hash\": \"82cbe1a3c9ebd477163676f720dcba8c4b86602667657b675863481fabae6b27\", \"block\": 42835440, \"value\": 9000000, \"netFee\": 345000, \"status\": 1, \"netUsage\": 0, \"energyFee\": 8296680, \"createTime\": {\"date\": {\"day\": 29, \"year\": 2022, \"month\": 7}, \"time\": {\"hour\": 15, \"nano\": 0, \"minute\": 59, \"second\": 51}}, \"energyUsage\": 0, \"contractAddress\": \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\", \"energyUsageTotal\": 29631, \"originEnergyUsage\": 0}], \"standardCurrency\": []}"));

        client.execute(httpPost);
    }


}