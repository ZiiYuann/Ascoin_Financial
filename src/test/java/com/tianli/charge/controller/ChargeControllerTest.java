package com.tianli.charge.controller;

import cn.hutool.json.JSONUtil;
import com.tianli.chain.dto.BaseTokenReq;
import com.tianli.chain.dto.DateReq;
import com.tianli.chain.dto.TimeReq;
import com.tianli.common.TimeUtils;
import com.tianli.tool.crypto.Crypto;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.crypto.util.DigestFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

class ChargeControllerTest {

    @Test
    void rechargeCallback() throws IOException {

        long l = TimeUtils.toTimestamp(LocalDateTime.now());
        String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp",l+"");

        BaseTokenReq baseTokenReq = new BaseTokenReq();
        baseTokenReq.setBlock("19848454");
        baseTokenReq.setHash("0x4aaff8705253d7eeeb60536328d93083710a81acc038d04f7e3ba9f584a9ece3");
        baseTokenReq.setFrom("0xfbf8b2f9f441f2cf929c2cb7b4de414d2913f73c");
        baseTokenReq.setTo("0x84c44d89d56930aee2f56230c01cbc95e6f8ed4b");
        baseTokenReq.setValue(new BigDecimal("100000000000000000000"));
        baseTokenReq.setContractAddress("0x55d398326f99059ff775485246999027b3197955");
        baseTokenReq.setDate(new DateReq(2022,7,22));
        baseTokenReq.setTime(new TimeReq(12,12,12,2000));
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("token", List.of(baseTokenReq));


        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/BSC");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l+ "");
        httpPost.setEntity(new StringEntity(JSONUtil.toJsonStr(hashMap)));

        client.execute(httpPost);
    }
}