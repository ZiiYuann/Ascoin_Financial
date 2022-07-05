package org.tron.common.utils;


import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class ByteArray {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static String toHexString(byte[] data) {
        return data == null ? "" : Hex.toHexString(data);
    }

    public static byte[] fromHexString(String data) {
        if (data == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (data.startsWith("0x")) {
            data = data.substring(2);
        }
        if (data.length() % 2 == 1) {
            data = "0" + data;
        }
        return Hex.decode(data);
    }

    public static long toLong(byte[] b) {
        if (b == null || b.length == 0) {
            return 0;
        }
        return new BigInteger(1, b).longValue();
    }

    public static byte[] fromString(String str) {
        if (str == null) {
            return null;
        }

        return str.getBytes();
    }

    public static String toStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        return new String(byteArray);
    }

    public static byte[] fromLong(long val) {
        return ByteBuffer.allocate(8).putLong(val).array();
    }

    /**
     * Generate a subarray of a given byte array.
     *
     * @param input the input byte array
     * @param start the start index
     * @param end the end index
     * @return a subarray of <tt>input</tt>, ranging from <tt>start</tt> (inclusively) to <tt>end</tt>
     * (exclusively)
     */
    public static byte[] subArray(byte[] input, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, end - start);
        return result;
    }

    /** 16进制能用到的所有字符 */
    private static String hexCharsStr = "0123456789ABCDEF";

    /** 16进制能用到的所有字符数组 */
    private static char[] hexCharsArr = hexCharsStr.toCharArray();

    /**
     * 0123456789ABCDEF -> 0 ~ 15
     */
    private static byte oneHexChar2Byte(char c) {
        byte b = (byte) hexCharsStr.indexOf(c);
        return b;
    }

    /**
     * 0 ~ 15 -> 0123456789ABCDEF
     */
    private static char byte2OneHexChar(byte b) {
        char c = hexCharsArr[b];
        return c;
    }

    /**
     * 两个16进制字符 -> 1个byte数值
     */
    private static byte twoHexChar2Byte(char high, char low) {
        byte b = (byte) (oneHexChar2Byte(high) << 4 | oneHexChar2Byte(low));
        return b;
    }


    /**
     * 1个byte数值 -> 两个16进制字符
     */
    public static char[] byte2TwoHexChar(byte b) {
        char[] chars = new char[2];

        // 高4位, 与操作 1111 0000
        byte high4bit = (byte) ((b & 0x0f0) >> 4);
        chars[0] = byte2OneHexChar((byte) high4bit);

        // 低四位, 与操作 0000 1111
        byte low4bit = (byte) (b & 0x0f);
        chars[1] = byte2OneHexChar((byte) low4bit);

        return chars;
    }

    /**
     * 文本字符串 -> 十六进制字符串
     */
    public static String str2HexString(String str) {
        byte[] bytes = str.getBytes();
        return bytes2HexString(bytes);
    }

    /**
     * byte数组 -> 十六进制字符串
     */
    public static final String bytes2HexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            char[] chars = byte2TwoHexChar(bytes[i]);
            sb.append(new String(chars));
        }
        return sb.toString();
    }

    /**
     * 十六进制字符串 -> 文本字符串
     */
    public static String hexString2Str(String hexStr) {
        byte[] bytes = hexString2Bytes(hexStr);
        return new String(bytes);
    }

    /**
     * 16进制的字符串 -> byte数组
     */
    public static byte[] hexString2Bytes(String hexStr) {
        int length = (hexStr.length() / 2);
        byte[] bytes = new byte[length];
        char[] charArr = hexStr.toCharArray();
        for (int i = 0; i < length; i++) {
            int position = i * 2;
            bytes[i] = twoHexChar2Byte(charArr[position], charArr[position + 1]);
        }
        return bytes;
    }

}
