package com.tianli.tool.crypto;


import com.tianli.exception.ErrCodeException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by wangqiyun on 2018/7/31.
 */
public class UrlEncode {
    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new ErrCodeException();
        }
    }
}
