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
//        HttpPost httpPost = new HttpPost("https://www.assureadd.com/api/charge/recharge/BSC");
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/TRON");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1744456642329086075, \"to\": \"TQDjgFcrVqpDpHJeh1T4WVBebdf4WKoqHQ\", " +
                "\"from\": \"TTahu848LG5HLPBmyzUwgG8v2XvCfrF8y9\", \"hash\": \"27a4fc47c9044a357f7b40a0ba261fed3dcec2a294a04685d74e12d46a004155\", \"block\": 44354678, \"value\": \"1000000\", \"netFee\": \"0\", \"status\": 1, \"netUsage\": 345, \"energyFee\": \"4102000\", \"createTime\": {\"date\": {\"day\": 20, \"year\": 2022, \"month\": 9}, \"time\": {\"hour\": 11, \"nano\": 0, \"minute\": 11, \"second\": 24}}, \"energyUsage\": 0, \"contractAddress\": \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\", \"energyUsageTotal\": 14650, \"originEnergyUsage\": 0}], \"standardCurrency\": []}"));
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