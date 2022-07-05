package com.tianli.tool.cache;

import com.google.gson.Gson;
import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Author wangqiyun
 * @Date 2019/7/29 10:47
 */
@Component("md5KeyGenerator")
public class Md5KeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object o, Method method, Object... objects) {
        String param = new Gson().toJson(objects);
        return Crypto.digestToString(DigestFactory.createMD5(), param);
    }
}
