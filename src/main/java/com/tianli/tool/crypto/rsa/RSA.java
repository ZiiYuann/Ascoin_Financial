package com.tianli.tool.crypto.rsa;

/**
 * Created by wangqiyun on 2017/4/5.
 */

import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

abstract public class RSA {


    /**
     * RSA签名
     *
     * @param content       待签名数据
     * @param privateKey    商户私钥
     * @param input_charset 编码格式
     * @return 签名值
     */
    public String sign(String content, String privateKey, Charset input_charset) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            return sign(content, priKey, input_charset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String signByte(byte[] content, String privateKey) {
        return signByte(content, getPrivateKey(privateKey));
    }

    public String signByte(byte[] content, PrivateKey privateKey) {
        try {
            java.security.Signature signature = java.security.Signature
                    .getInstance(getSign_algorithms());

            signature.initSign(privateKey);
            signature.update(content);

            byte[] signed = signature.sign();

            return new String(Base64.encode(signed));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String sign(String content, PrivateKey privateKey, Charset input_charset) {
        try {
            java.security.Signature signature = java.security.Signature
                    .getInstance(getSign_algorithms());

            signature.initSign(privateKey);
            signature.update(content.getBytes(input_charset));

            byte[] signed = signature.sign();

            return new String(Base64.encode(signed));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA验签名检查
     *
     * @param content        待签名数据
     * @param sign           签名值
     * @param ali_public_key 支付宝公钥
     * @param input_charset  编码格式
     * @return 布尔值
     */
    public boolean verify(String content, String sign, String ali_public_key, Charset input_charset) {
        try {
            PublicKey pubKey = getPublicKey(ali_public_key);
            return verify(content, sign, pubKey, input_charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verify(String content, String sign, PublicKey pubKey, Charset input_charset) {
        try {
            java.security.Signature signature = java.security.Signature
                    .getInstance(getSign_algorithms());
            signature.initVerify(pubKey);
            signature.update(content.getBytes(input_charset));
            return signature.verify(Base64.decode(sign));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public static byte[] encrypt(byte[] content, PublicKey pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ecb/pkcs1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(content);
    }

    public static byte[] encrypt(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ecb/pkcs1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    public static byte[] encrypt(byte[] content, String pubKey) {
        try {
            return encrypt(content, getPublicKey(pubKey));
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] encryptByPrivateKey(byte[] content, String privateKey) {
        try {
            return encrypt(content, getPrivateKey(privateKey));
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decrypt(byte[] content, PublicKey pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ecb/pkcs1Padding");
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(content);
    }

    public static byte[] decrypt(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ecb/pkcs1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    public static byte[] decrypt(byte[] content, String privateKey) {
        try {
            return decrypt(content, getPrivateKey(privateKey));
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decryptByPublicKey(byte[] content, String pubKey) {
        try {
            return decrypt(content, getPublicKey(pubKey));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解密
     *
     * @param content       密文
     * @param private_key   商户私钥
     * @param input_charset 编码格式
     * @return 解密后的字符串
     */
    public static String decrypt(String content, String private_key, String input_charset) throws Exception {
        PrivateKey prikey = getPrivateKey(private_key);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);

        InputStream ins = new ByteArrayInputStream(Base64.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        //rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
        byte[] buf = new byte[128];
        int bufl;

        while ((bufl = ins.read(buf)) != -1) {
            byte[] block = null;

            if (buf.length == bufl) {
                block = buf;
            } else {
                block = new byte[bufl];
                for (int i = 0; i < bufl; i++) {
                    block[i] = buf[i];
                }
            }

            writer.write(cipher.doFinal(block));
        }

        return new String(writer.toByteArray(), input_charset);
    }


    /**
     * 得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     */
    public static PrivateKey getPrivateKey(String key) {
        try {

            byte[] keyBytes;

            keyBytes = Base64.decode(key);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            return null;
        }
    }

    public static PublicKey getPublicKey(String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(key);
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (Exception e) {
            return null;
        }
    }

    public abstract String getSign_algorithms();
}
