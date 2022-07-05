package com.tianli.tool;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author wangqiyun
 * @Date 2018/12/7 11:30 PM
 */
public class RandomStringGeneral {
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    public static String general() {
        return general(43);
    }

    public static String general(int num) {
        SecureRandom random;
        random = new SecureRandom();
        char[] result = new char[num];
        for (int i = 0; i < num; i++)
            result[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        return new String(result);
    }

    public static String common(int num) {
        char[] result = new char[num];
        for (int i = 0; i < num; i++)
            result[i] = ALPHABET[ThreadLocalRandom.current().nextInt(ALPHABET.length)];
        return new String(result);
    }

    public static String randomint() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
    }
}
