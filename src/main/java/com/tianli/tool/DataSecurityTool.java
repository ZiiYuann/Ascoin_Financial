package com.tianli.tool;

import com.google.gson.Gson;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.crypto.Base64;
import com.tianli.tool.crypto.ecdsa.ECDSA;
import com.tianli.tool.crypto.ecdsa.Secp256k1;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@Service
public class DataSecurityTool {

    public static final Secp256k1 SECP256K1 = Secp256k1.getInstance();

    public static void main(String args[]) throws Exception {
        BCECPrivateKey bcecPrivateKey = SECP256K1.privateKey(new BigInteger("a07a0beb642c7d53c16cf03584307df828ad8b83ee6d33b370fd630e2c471306", 16));
        ECDSA.Eccrypto eccrypto = new Gson().fromJson(" {\"iv\":\"01e7225cd619c6bce0c11eaf3740b680\",\"ephemPublicKey\":\"04adaca3acd8f397abf2608db65b176d78d375fcf798db535db931130e69d9afa716c2512a2b4d8af009da0e1c6a892ab2ced0a0d7c67caad2253e794b75123b7d\",\"ciphertext\":\"1897f5bf7624e8abda9ab20eb6059533db3b65cdac1ec0d8c861902572ad0905867d4f82d6152444455b822a69a9fc7e7347fcce16bc369b89184c7c2adb5aaba7c76b8e42ae0848b2e3f0ef02866a58\",\"mac\":\"f98fe8bcb8a72cb7af68dc62b465f33e185d8f216d72fd6156f98aa066aeabf8\"}", ECDSA.Eccrypto.class);
        String s = SECP256K1.decryptWithPrivateKey(bcecPrivateKey, eccrypto);
        System.out.println(s);
    }

    @Value("${secret.ecdsa.privateKey}")
    private String privateKey;
//    public static String privateKey() {
//        DataSecurityProperties dataSecurityProperties = ApplicationContextTool.getBean(DataSecurityProperties.class);
//        if (Objects.isNull(dataSecurityProperties)) {
//            ErrorCodeEnum.NOT_OPEN.throwException();
//        }
//        return dataSecurityProperties.getPrivateKey();
//    }
    public String privateKey() {
        return privateKey;
    }

    public BigInteger bigIntegerPrivateKey() {
        String privateKey = privateKey();
        if (StringUtils.isBlank(privateKey)) ErrorCodeEnum.NOT_OPEN.throwException();
        return new BigInteger(privateKey, 16);
    }

    public BigInteger bigIntegerPublicKey() {
        BigInteger privateKey = bigIntegerPrivateKey();
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        return ecKeyPair.getPublicKey();
    }

    public String publicKey() {
        BigInteger bigIntegerPublicKey = bigIntegerPublicKey();
        if (Objects.isNull(bigIntegerPublicKey)) ErrorCodeEnum.NOT_OPEN.throwException();
        return bigIntegerPublicKey.toString(16);
    }

    public String decryptWithPrivateKey(String encryptStr){
        try {
            return decryptWithPrivateKey_(encryptStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public String decryptWithPrivateKey_(String encryptStr) throws Exception {
        BCECPrivateKey bcecPrivateKey = SECP256K1.privateKey(this.bigIntegerPrivateKey());
        ECDSA.Eccrypto eccrypto = new Gson().fromJson(encryptStr, ECDSA.Eccrypto.class);
        return SECP256K1.decryptWithPrivateKey(bcecPrivateKey, eccrypto);
    }

    public String encryptWithPublicKey(String plaintextStr){
        try {
            return encryptWithPublicKey_(plaintextStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String encryptWithPublicKey_(String plaintextStr) throws Exception {
        BCECPublicKey bcecPublicKey = SECP256K1.publicKey(this.bigIntegerPublicKey());
        ECDSA.Eccrypto eccrypto = SECP256K1.encryptWithPublicKey(bcecPublicKey, plaintextStr);
        if (Objects.isNull(eccrypto)) return null;
        return new Gson().toJson(eccrypto);
    }
}
