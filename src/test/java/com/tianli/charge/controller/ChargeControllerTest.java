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


        HttpClient client = HttpClientBuilder.create().build();
        // BSC链
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/api/charge/recharge/BSC");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Sign", sign);
        httpPost.setHeader("timestamp", l+ "");
        httpPost.setEntity(new StringEntity("{\"token\": [{\"id\": 1739393333552857549, \"to\": \"0x0203fad8e9bf8f4c4c98adff6b5d29d68cdd7454\", \"from\": \"0xe208d2fb37df02061b78848b83f02b4ad33540e4\", \"hash\": \"0xe61e173839f83bceea54e0f465b8c204662cd28e1b87bdb5a25ae94fbf422fbc\", \"block\": 19879916, \"value\": 1000000000000000, \"createTime\": {\"date\": {\"day\": 26, \"year\": 2022, \"month\": 7}, \"time\": {\"hour\": 13, \"nano\": 0, \"minute\": 52, \"second\": 18}}, \"contractAddress\": \"0x55d398326f99059ff775485246999027b3197955\"}], \"standardCurrency\": []}\n"));

        client.execute(httpPost);
    }
}