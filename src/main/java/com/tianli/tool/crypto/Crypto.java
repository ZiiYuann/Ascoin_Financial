package com.tianli.tool.crypto;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.encrypt.BouncyCastleAesGcmBytesEncryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotBlank;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Created by wangqiyun on 2018/1/16.
 */
public class Crypto {
    public static byte[] hmac(Digest digest, String key, String text) {
        if (text == null) text = "";
        byte[] textBytes = Utf8.encode(text);
        return hmac(digest, key, textBytes);
    }

    public static byte[] hmac(Digest digest, byte[] key, String text) {
        HMac hMac = new HMac(digest);
        hMac.init(new KeyParameter(key));
        byte[] textBytes = Utf8.encode(text);
        hMac.update(textBytes, 0, textBytes.length);
        int size = hMac.getMacSize();
        byte[] result = new byte[size];
        hMac.doFinal(result, 0);
        return result;
    }

    public static byte[] hmac(Digest digest, String key, byte[] textBytes) {
        if (textBytes == null) textBytes = new byte[0];
        HMac hMac = new HMac(digest);
        hMac.init(new KeyParameter(Utf8.encode(key)));
        hMac.update(textBytes, 0, textBytes.length);
        int size = hMac.getMacSize();
        byte[] result = new byte[size];
        hMac.doFinal(result, 0);
        return result;
    }

    public static String hmacToString(Digest digest, String key, String text) {
        return new String(Hex.encode(hmac(digest, key, text)));
    }

    public static String hmacToString(Digest digest, String key, byte[] textBytes) {
        return new String(Hex.encode(hmac(digest, key, textBytes)));
    }

    public static String aes_gcm_encrypt(@NotBlank String passsword, @NotBlank String salt, @NotBlank String plain_text) {
        BouncyCastleAesGcmBytesEncryptor encryptor = new BouncyCastleAesGcmBytesEncryptor(passsword, salt);
        return new String(Hex.encode(encryptor.encrypt(Utf8.encode(plain_text))));
    }

    public static String aes_gcm_decrypt(@NotBlank String passsword, @NotBlank String salt, @NotBlank String security_text) {
        BouncyCastleAesGcmBytesEncryptor encryptor = new BouncyCastleAesGcmBytesEncryptor(passsword, salt);
        return Utf8.decode(encryptor.decrypt(Hex.decode(security_text)));
    }


    public static byte[] aes_cbc_encrypt(byte[] secret, byte[] content) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            return cipher.doFinal(content);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] aes_ecb_encrypt(byte[] secret, byte[] content) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(content);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] aes_ecb_decrypt(byte[] secret, byte[] content) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return cipher.doFinal(content);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] aes_cbc_decrypt(byte[] secret, byte[] content) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            return cipher.doFinal(content);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] digest(Digest digest, String text) {
        byte[] str = Utf8.encode(text);
        digest.update(str, 0, str.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }

    public static String digestToString(Digest digest, String text) {
        return new String(Hex.encode(digest(digest, text)));
    }

    public static byte[] hmac(Digest digest, byte[] key, byte[] textBytes) {
        if (textBytes == null) textBytes = new byte[0];
        HMac hMac = new HMac(digest);
        hMac.init(new KeyParameter(key));
        hMac.update(textBytes, 0, textBytes.length);
        int size = hMac.getMacSize();
        byte[] result = new byte[size];
        hMac.doFinal(result, 0);
        return result;
    }

    public static byte[] digest(Digest digest, byte[] str) {
        digest.update(str, 0, str.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }

    public static byte[] aes_cbc_encrypt(byte[] secret, byte[] content, byte[] iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(content);
    }
    public static byte[] aes_cbc_decrypt(byte[] secret, byte[] content, byte[] iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(content);
    }
    public static byte[] aes_gcm_encrypt(byte[] secret, byte[] content) throws Exception {
        return aes_gcm_encrypt(secret, content, null);
    }

    public static byte[] aes_gcm_encrypt(byte[] secret, byte[] content, byte[] associatedData) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
        Cipher cipher = Cipher.getInstance("AES_256/GCM/NoPadding");
        byte[] iv = new byte[32];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);
        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }
        byte[] cipherText = cipher.doFinal(content);
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        return byteBuffer.array();
    }

    public static byte[] aes_gcm_decrypt(byte[] secret, byte[] content) throws Exception {
        return aes_gcm_decrypt(secret, content, null);
    }

    public static byte[] aes_gcm_decrypt(byte[] secret, byte[] content, byte[] associatedData) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
        Cipher cipher = Cipher.getInstance("AES_256/GCM/NoPadding");
        AlgorithmParameterSpec gcmIv = new GCMParameterSpec(128, content, 0, 32);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmIv);
        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }
        return cipher.doFinal(content, 32, content.length - 32);
    }

}
