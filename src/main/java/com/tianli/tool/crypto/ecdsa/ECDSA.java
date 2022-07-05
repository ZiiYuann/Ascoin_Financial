package com.tianli.tool.crypto.ecdsa;

import com.tianli.tool.crypto.Crypto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.javatuples.Pair;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.web3j.utils.Numeric;

import javax.crypto.KeyAgreement;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

/**
 * @author wangqiyun
 * @since 2022/4/9 19:38
 */
abstract public class ECDSA {//npm: eth-crypto  //npm: node-crypto-gcm
    public X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName(group());
    public ECDomainParameters CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH(), CURVE_PARAMS.getSeed());
    public ECNamedCurveSpec ecNamedCurveSpec = new ECNamedCurveSpec(group(), CURVE.getCurve(), CURVE.getG(), CURVE.getN(), CURVE.getH(), CURVE.getSeed());

    abstract public String group();

    abstract public int length();

    public BCECPrivateKey privateKey(BigInteger ethPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("ECDSA");
        PrivateKey privateKey = kf.generatePrivate(new ECPrivateKeySpec(ethPrivateKey, ecNamedCurveSpec));
        return (BCECPrivateKey) privateKey;
    }

    public BCECPublicKey publicKey(BigInteger ethPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("ECDSA");
        byte[] point = Numeric.toBytesPadded(ethPublicKey, length() / 4);
        int len = length() / 8;
        PublicKey publicKey = kf.generatePublic(new ECPublicKeySpec(new ECPoint(Numeric.toBigInt(point, 0, len), Numeric.toBigInt(point, len, len)), ecNamedCurveSpec));
        return (BCECPublicKey) publicKey;
    }

    public KeyPair general() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(group());
        ECParameterSpec ecParameterSpec = EC5Util.convertToSpec(CURVE_PARAMS);
        keyPairGenerator.initialize(ecParameterSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    public Pair<BigInteger, BigInteger> generateSignature(BCECPrivateKey bcecPrivateKey, byte[] hash) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(toBigInteger(bcecPrivateKey), CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(hash);
        return Pair.with(components[0], components[1]);
    }

    public boolean verifySignature(BCECPublicKey publicKey, byte[] hash, Pair<BigInteger, BigInteger> sign) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), CURVE);
        signer.init(false, publicKeyParameters);
        return signer.verifySignature(hash, sign.getValue0(), sign.getValue1());
    }

    public Keys generalKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = general();
        return Keys.builder().privateKey((BCECPrivateKey) keyPair.getPrivate()).publicKey((BCECPublicKey) keyPair.getPublic()).build();
    }

    public byte[] encryptWithAesGcm(BCECPrivateKey bcecPrivateKey, BCECPublicKey bcecPublicKey, byte[] plaintext) throws Exception {
        byte[] secret = genSecret256(bcecPrivateKey, bcecPublicKey);
        return Crypto.aes_gcm_encrypt(secret, plaintext);
    }

    public byte[] decryptWithAesGcm(BCECPrivateKey bcecPrivateKey, BCECPublicKey bcecPublicKey, byte[] ciphertext) throws Exception {
        byte[] secret = genSecret256(bcecPrivateKey, bcecPublicKey);
        return Crypto.aes_gcm_decrypt(secret, ciphertext);
    }

    public byte[] genSecret256(BCECPrivateKey bcecPrivateKey, BCECPublicKey bcecPublicKey) throws Exception {
        byte[] keyAgreement = keyAgreement(bcecPrivateKey, bcecPublicKey);
        return Crypto.digest(DigestFactory.createSHA3_256(), keyAgreement);
    }

    public Eccrypto encryptWithPublicKey(BCECPublicKey bcecPublicKey, String plaintext) throws Exception {
//        BCECPublicKey bcecPublicKey = publicKey(publicKey);
        KeyPair keyPair = general();
        BCECPrivateKey ephemPrivateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey ephemPublicKey = (BCECPublicKey) keyPair.getPublic();
        byte[] secret = keyAgreement(ephemPrivateKey, bcecPublicKey);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        byte[] hash = Crypto.digest(DigestFactory.createSHA512(), secret);
        byte[] encryptionKey = ByteUtils.subArray(hash, 0, 32);
        byte[] macKey = ByteUtils.subArray(hash, 32);
        byte[] ciphertext = Crypto.aes_cbc_encrypt(encryptionKey, plaintext.getBytes(StandardCharsets.UTF_8), iv);
        byte[] ephemPublicKeyEncode = ephemPublicKey.getQ().getEncoded(false);
        byte[] mac = Crypto.hmac(DigestFactory.createSHA256(), macKey, ByteUtils.concatenate(iv, ByteUtils.concatenate(ephemPublicKeyEncode, ciphertext)));
        return Eccrypto.builder().iv(new String(Hex.encode(iv))).ephemPublicKey(new String(Hex.encode(ephemPublicKeyEncode))).ciphertext(new String(Hex.encode(ciphertext))).mac(new String(Hex.encode(mac))).build();
    }

    public String decryptWithPrivateKey(BCECPrivateKey bcecPrivateKey, Eccrypto eccrypto) throws Exception {
//        BCECPrivateKey bcecPrivateKey = privateKey(privateKey);
        BCECPublicKey bcecPublicKey = publicKey(Numeric.toBigInt(eccrypto.getEphemPublicKey().substring(2)));
        byte[] secret = keyAgreement(bcecPrivateKey, bcecPublicKey);
        byte[] hash = Crypto.digest(DigestFactory.createSHA512(), secret);
        byte[] encryptionKey = ByteUtils.subArray(hash, 0, 32);
        byte[] macKey = ByteUtils.subArray(hash, 32);
        byte[] hmac = Crypto.hmac(DigestFactory.createSHA256(), macKey, Hex.decode(eccrypto.getIv() + eccrypto.getEphemPublicKey() + eccrypto.getCiphertext()));
        if (!eccrypto.getMac().equals(new String(Hex.encode(hmac)))) {
            throw new RuntimeException("decryptWithPrivateKey ERROR!");
        }
        byte[] plaintext = Crypto.aes_cbc_decrypt(encryptionKey, Hex.decode(eccrypto.getCiphertext()), Hex.decode(eccrypto.getIv()));
        return Utf8.decode(plaintext);
    }

    public BigInteger toBigInteger(Key key) {
        if (key instanceof BCECPrivateKey) {
            return ((BCECPrivateKey) key).getD();
        } else if (key instanceof BCECPublicKey) {
            byte[] publicKeyBytes = ((BCECPublicKey) key).getQ().getEncoded(false);
            return new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        }
        return Numeric.toBigInt(key.getEncoded());
    }

    public String toHexString(Key key) {
        BigInteger bigInteger = toBigInteger(key);
        return Numeric.toHexStringNoPrefix(bigInteger);
    }

    public byte[] keyAgreement(BCECPrivateKey privateKey, BCECPublicKey publicKey) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(privateKey);
        ka.doPhase(publicKey, true);
        return ka.generateSecret();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Eccrypto {
        private String iv;
        private String ephemPublicKey;
        private String ciphertext;
        private String mac;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class Keys {
        private BCECPrivateKey privateKey;
        private BCECPublicKey publicKey;
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

}
