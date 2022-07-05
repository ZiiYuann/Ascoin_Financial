package org.tron.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.Base58;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.tron.common.crypto.Sha256Sm3Hash;
import org.web3j.crypto.Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chensong
 * @date 2021-05-20 15:38
 * @since 1.0.0
 */
public class Base58Utils {

    public static String encode58Check(byte[] input){
        byte[] hash0 = Sha256Sm3Hash.hash(input);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        byte[] inputCheck = new byte[input.length + 4];
        System.arraycopy(input, 0, inputCheck, 0, input.length);
        System.arraycopy(hash1, 0, inputCheck, input.length, 4);
        return Base58.encode(inputCheck);
    }

    public static byte[] decode58Check(String input) {
        byte[] decodeCheck = Base58.decode(input);
        if (decodeCheck == null) ErrorCodeEnum.throwException("合约地址错误");
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = Sha256Sm3Hash.hash(decodeData);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        if (hash1[0] == decodeCheck[decodeData.length]
                && hash1[1] == decodeCheck[decodeData.length + 1]
                && hash1[2] == decodeCheck[decodeData.length + 2]
                && hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    public static String delZeroForNum(String str) {

        return str.replaceAll("^(0+)", "");
    }

    public static List<String> getStrList(String inputString, int length) {

        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {

            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    public static List<String> getStrList(String inputString, int length,
                                          int size) {

        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {

            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    public static String substring(String str, int f, int t) {

        if (f > str.length())
            return null;
        if (t > str.length()) {

            return str.substring(f, str.length());
        } else {

            return str.substring(f, t);
        }
    }

    public static byte[] decodeFromBase58Check(String addressBase58) {
        if (StringUtils.isEmpty(addressBase58)) {
            return null;
        }
        byte[] address = decode58Check(addressBase58);
        if (!addressValid(address)) {
            return null;
        }
        return address;
    }

    public static boolean addressValid(byte[] address) {
        if (ArrayUtils.isEmpty(address)) {
            return false;
        }
        if (address.length != 21) {
            return false;
        }
        byte preFixbyte = address[0];
        if (preFixbyte != 65) {
            return false;
        }
        // Other rule;
        return true;
    }

    /**
     * 合约地址 +22
     * @param methodSign
     * @param input
     * @param isHex
     * @return
     */
    public static String parseMethod(String methodSign, String input, boolean isHex) {
        byte[] selector = new byte[4];
        System.arraycopy(Hash.sha3(methodSign.getBytes()), 0, selector,0, 4);
//        System.out.println(methodSign + ":" + org.spongycastle.util.encoders.Hex.toHexString(selector));
        if (input.length() == 0) {
            return org.spongycastle.util.encoders.Hex.toHexString(selector);
        }
        if (isHex) {
            return org.spongycastle.util.encoders.Hex.toHexString(selector) + input;
        }
        byte[] encodedParms = encodeInput(input);

        return org.spongycastle.util.encoders.Hex.toHexString(selector) + org.spongycastle.util.encoders.Hex.toHexString(encodedParms);
    }

    private static byte[] encodeInput(String input) {
        ObjectMapper mapper = new ObjectMapper();
        input = "[" + input + "]";
        List items;
        try {
            items = mapper.readValue(input, List.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Coder coder = new CoderAddress();
        return coder.encode(items.get(0).toString());
    }

    static class CoderAddress extends Coder {

        @Override
        byte[] encode(String value) {
            byte[] address = Base58Utils.decodeFromBase58Check(value);
            if (address == null) {
                return null;
            }
            byte[] data = new byte[32];
            if (address.length == 32){
                data = address;
            }else if (address.length <= 32){
                System.arraycopy(address, 0, data, 32 - address.length, address.length);
            }
            return data;
        }

        @Override
        byte[] decode() {
            return new byte[0];
        }
    }

    static abstract class Coder {
        boolean dynamic = false;
        //    DataWord[] encode
        abstract byte[] encode(String value);
        abstract byte[] decode();

    }

}
