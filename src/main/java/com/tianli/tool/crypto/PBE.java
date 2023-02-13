package com.tianli.tool.crypto;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-29
 **/
public class PBE {

    private static final String KEY_NAME = "PBEWITHMD5andDES";

    public static String encryptBase64(byte[] salt, String password, String content) {
        return Base64.encodeBase64String(encrypt(salt, password, content));
    }

    public static byte[] encrypt(byte[] salt, String password, String content) {

        try {
            var secretKeyFactory = SecretKeyFactory.getInstance(KEY_NAME);
            var cipher = Cipher.getInstance(KEY_NAME);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            Key key = secretKeyFactory.generateSecret(pbeKeySpec);
            PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParameterSpec);
            return cipher.doFinal(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptBase64(byte[] salt, String password, String content) {
        return decrypt(salt, password, Base64.decodeBase64(content));
    }

    public static String decrypt(byte[] salt, String password, byte[] content) {
        try {
            var secretKeyFactory = SecretKeyFactory.getInstance(KEY_NAME);
            var cipher = Cipher.getInstance(KEY_NAME);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            Key key = secretKeyFactory.generateSecret(pbeKeySpec);
            PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParameterSpec);
            byte[] result = cipher.doFinal(content);
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] salt = random.generateSeed(8);
        System.out.println(Arrays.toString(salt));
    }
}
