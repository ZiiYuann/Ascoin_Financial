package com.tianli.chain.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.chain.dto.DesignBtcTx;
import com.tianli.chain.enums.ChainType;
import com.tianli.common.ConfigConstants;
import com.tianli.common.HttpUtils;
import com.tianli.exception.ErrCodeException;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.crypto.Crypto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author cs
 * @Date 2022-12-28 14:44
 */@Slf4j
@Service
public class UutokenHttpService {

    @Resource
    private ConfigService configService;

    public void registerBtcAddress(String address) {
        Map<String, String> body = Map.of(
                "address", address,
                "chain", ChainType.BTC.name());
        JSONObject response = post("/api/address/watch/financial", body, true);
        String code = response.get("code", String.class);
        if(!"0".equals(code)) {
            log.error("注册btc地址失败 {}", address);
            throw new ErrCodeException("注册btc地址失败 " + address);
        }
    }

    public DesignBtcTx designSimpleBitcoin(String from, String to, Long value) {
        Map<String, String> body = Map.of(
                "from_address", from,
                "to_address", to,
                "value", value.toString()
        );
        JSONObject response = post("/api/tx/design/simple/bitcoin", body, false);
        return response.get("data", DesignBtcTx.class);
    }

    public String btcBalance(String address) {
        JSONObject response = get("/api/address/balance/detail?address=" + address);
        return response.getJSONObject("data").get("sumTxBalance", String.class);
    }

    private JSONObject post(String path, Map<String, String> body, boolean signed) {
        String host = configService.getOrDefault(ConfigConstants.UUTOKEN_HOST, "https://uutoken.giantdt.com");
        try {
            HttpResponse response = HttpUtils.doPost(host, path, null, getHeader(signed), Map.of(), body);
            return JSONUtil.parseObj(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrCodeException("http 调用异常");
        }
    }

    private JSONObject get(String path) {
        String host = configService.getOrDefault(ConfigConstants.UUTOKEN_HOST, "https://uutoken.giantdt.com");
        try {
            HttpResponse response = HttpUtils.doGet(host, path, null, Map.of(), Map.of());
            return JSONUtil.parseObj(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrCodeException("http 调用异常");
        }
    }

    public Map<String, String> getHeader(boolean signed) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        if(signed) {
            long currentTimeMillis = System.currentTimeMillis();
            header.put("timestamp", String.valueOf(currentTimeMillis));
            header.put("sign", Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", String.valueOf(currentTimeMillis)));
        }
        return header;
    }
}
