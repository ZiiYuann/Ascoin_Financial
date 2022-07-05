package com.tianli.tool.crypto;

import org.springframework.security.crypto.codec.Utf8;


/**
 * Created by wangqiyun on 2018/1/10.
 */
public class Base64 {
    public static String encode(String str) {
        return Utf8.decode(org.bouncycastle.util.encoders.Base64.encode(Utf8.encode(str)));
    }

    public static String encode(byte[] bytes) {
        return Utf8.decode(org.bouncycastle.util.encoders.Base64.encode(bytes));
    }

    public static String decode(String str) {
        return Utf8.decode(org.bouncycastle.util.encoders.Base64.decode(Utf8.encode(str)));
    }

    public static String decode(byte[] bytes) {
        return Utf8.decode(org.bouncycastle.util.encoders.Base64.decode(bytes));
    }

    public static byte[] encodeToByte(String str) {
        return org.bouncycastle.util.encoders.Base64.encode(Utf8.encode(str));
    }

    public static byte[] encodeToByte(byte[] bytes) {
        return org.bouncycastle.util.encoders.Base64.encode(bytes);
    }

    public static byte[] decodeToByte(String str) {
        return org.bouncycastle.util.encoders.Base64.decode(Utf8.encode(str));
    }

    public static byte[] decodeToByte(byte[] bytes) {
        return org.bouncycastle.util.encoders.Base64.decode(bytes);
    }
}
