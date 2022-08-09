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
        HttpPost httpPost = new HttpPost("https://www.assureadd.com//api/charge/recharge/TRON");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1740647482801496358, \"to\": \"TVi7uDV8Nw3RT4prbtmSzJnny53VYAXbzY\", \"from\": \"TTahu848LG5HLPBmyzUwgG8v2XvCfrF8y9\", \"hash\": \"fd25a6c32b33438d0757ab4c9ee261c6a3e2cc3ccb6418889492b054ebb68556\", \"block\": 43144439, \"value\": 9888888, \"netFee\": 0, \"status\": 1, \"netUsage\": 345, \"energyFee\": 8302000, \"createTime\": {\"date\": {\"day\": 9, \"year\": 2022, \"month\": 8}, \"time\": {\"hour\": 10, \"nano\": 0, \"minute\": 6, \"second\": 27}}, \"energyUsage\": 0, \"contractAddress\": \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\", \"energyUsageTotal\": 29650, \"originEnergyUsage\": 0}], \"standardCurrency\": []}"));
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