package com.tianli.common;

import com.tianli.tool.crypto.UrlEncode;
import com.tianli.tool.crypto.rsa.SHA256WithRSA;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.TreeMap;

import static com.tianli.exception.ErrorCodeEnum.SECRET_ERROR;

/**
 * @Author wangqiyun
 * @Date 2018/12/24 4:54 PM
 */
public class AliPaySign {
    public static String signString(Map<String, Object> param, String... remove) {
        TreeMap<String, Object> treeMap = new TreeMap<>(param);
        treeMap.remove("sign");
        for (String str : remove)
            treeMap.remove(str);
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            String key = entry.getKey(), value = (entry.getValue() == null ? null : entry.getValue().toString());
            if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                if (first) first = false;
                else result.append("&");
                result.append(key).append("=").append(value);
            }
        }
        return result.toString();
    }

    public static String sign(Map<String, Object> param, PrivateKey privateKey) {
        if (privateKey == null) SECRET_ERROR.throwException();
        String signString = signString(param);
        String sign = new SHA256WithRSA().sign(signString, privateKey, StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(sign)) SECRET_ERROR.throwException();
        return sign;
    }

    public static boolean verify(Map<String, Object> param, PublicKey publicKey) {
        if (publicKey == null) SECRET_ERROR.throwException();
        String sign = param.get("sign").toString();
        if (StringUtils.isEmpty(sign)) return false;
        String signString = signString(param, "sign_type");
        return new SHA256WithRSA().verify(signString, sign, publicKey, StandardCharsets.UTF_8);
    }

    public static String toKeyAndEncodeValue(Map<String, Object> param) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            if (first) first = false;
            else result.append("&");
            result.append(entry.getKey()).append("=").append(UrlEncode.encode(entry.getValue().toString()));
        }
        return result.toString();
    }
}
