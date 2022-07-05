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
import java.util.Objects;

@Service
public class DataSecurityTool {

    public static final Secp256k1 SECP256K1 = Secp256k1.getInstance();

    public static void main(String args[]) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        System.out.println(Base64.encode(SECP256K1.generalKey().getPrivateKey().getS().toString()));
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
        return Base64.decode(privateKey);
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
