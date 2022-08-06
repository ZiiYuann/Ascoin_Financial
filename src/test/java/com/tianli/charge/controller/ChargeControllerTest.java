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
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/ETH");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l + "");
        httpPost.setEntity(new StringEntity("{\"token\":[],\"standardCurrency\":[{\"id\":1740301064781596700,\"to\":\"0x09abcb496fbbcd3f43ebf05e7256114e71feb7e3\",\"from\":\"0x504958caa2488691d85d5b36670dc9411f8dc383\",\"hash\":\"0x0a17097701f087e956c86595cbfeb403c74b73158f8682d532cfe9cf4858f95d\",\"block\":15280576,\"nonce\":6,\"value\":4000000000000000,\"status\":1,\"gasUsed\":21000,\"gasPrice\":10000000000,\"createTime\":{\"date\":{\"day\":5,\"year\":2022,\"month\":8},\"time\":{\"hour\":14,\"nano\":0,\"minute\":19,\"second\":53}}}]}"));
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