package com.tianli.tool;

import com.tianli.tool.crypto.TweetNaclFast;
import com.tianli.tool.crypto.fil.Blake2b;
import com.tianli.tool.crypto.schnorrkel.sign.KeyPair;
import com.tianli.tool.crypto.schnorrkel.sign.SigningContext;
import com.tianli.tool.crypto.schnorrkel.sign.SigningTranscript;
import com.tianli.tool.crypto.schnorrkel.ss58.SS58Codec;
import com.tianli.tool.crypto.schnorrkel.ss58.SS58Type;
import lombok.extern.slf4j.Slf4j;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.data.redis.util.ByteUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import party.loveit.bip44forjava.utils.Bip44Utils;
import party.loveit.bip44forjava.utils.Utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * @Author cs
 * @Date 2022-03-23 1:54 下午
 */
@Slf4j
public class AddressVerifyUtils {

    private static byte[] CID_PREFIX = new byte[]{0x01, 0x71, (byte) 0xa0, (byte) 0xe4, 0x02, 0x20};
    private static byte[] SIGNING_CONTEXT = "substrate".getBytes();

    public static void main(String[] args) {
        String password = "dutch squirrel expect promote captain hub buzz into pig spy tiger enact";
        System.out.println("pwd:\n" + password);

        BigInteger privateKey = Bip44Utils.getPathPrivateKey(Collections.singletonList(password), "m/44'/60'/0'/0/0");
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);

        String walletAddress = Numeric.prependHexPrefix(Keys.getAddress(ecKeyPair.getPublicKey()));
        System.out.println("address:\n" + walletAddress);


        String sign = ethSignMessage("1234567890", password);
        System.out.println("sign:\n" + sign);



        String address = ethSignedToAddress(sign, "1234567890");
        System.out.println("ethSignedToAddress:\n" + address);

        System.out.println("check address: " + address.equalsIgnoreCase(walletAddress));
    }

    public static String ethSignMessage(String message, String password) {
        BigInteger privateKey = Bip44Utils.getPathPrivateKey(Collections.singletonList(password), "m/44'/60'/0'/0/0");
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        return ethSignMessage(message, ecKeyPair);
    }

    public static String ethSignMessage(String message, BigInteger privateKey) {
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        return ethSignMessage(message, ecKeyPair);
    }

    public static String ethSignMessage(String message, ECKeyPair ecKeyPair) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] sb = new byte[65];
        Sign.SignatureData signatureData = Sign.signPrefixedMessage(messageBytes, ecKeyPair);
        System.arraycopy(signatureData.getR(),0, sb, 0, 32);
        System.arraycopy(signatureData.getS(),0, sb, 32, 32);
        System.arraycopy(signatureData.getV(),0, sb, 64, 1);
        return Hex.toHexString(sb);
    }


    public static BigInteger ethSignedToPubKey(String sign, String msg) {
        try {
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
//            byte[] decode = Hex.decode(sign);
            byte[] decode = Numeric.hexStringToByteArray(sign);
            byte[] r = Arrays.copyOfRange(decode, 0, 32);
            byte[] s = Arrays.copyOfRange(decode, 32, 64);
            byte v = decode[64];
            if (v < 27) {
                v += 27;
            }
            Sign.SignatureData data = new Sign.SignatureData(v, r, s);
            return Sign.signedPrefixedMessageToKey(bytes, data);
        } catch (Exception e) {
            log.error("web3j验签失败 =====> sign:" + sign + "  =======> msg:" + msg, e);
            return null;
        }
    }

    public static String ethSignedToAddress(String sign, String msg) {
        BigInteger pubKey = ethSignedToPubKey(sign, msg);
        if (Objects.isNull(pubKey)) return null;
        return Numeric.prependHexPrefix(Keys.getAddress(pubKey));
    }

    public static String ethSignedToAddress(BigInteger pubKey) {
        if (Objects.isNull(pubKey)) return null;
        return Numeric.prependHexPrefix(Keys.getAddress(pubKey));
    }

    public static String tronSignedToAddress(String sign, String msg) {
        String ethAddress = ethSignedToAddress(sign, msg);
        if (StringUtils.isNotBlank(ethAddress)) {
            try {
                byte[] hexBytes = Hex.decode(ethAddress.replace("0x", "41"));
//                byte[] hexBytes = Numeric.hexStringToByteArray(sign);
                return Base58.encodeChecked(hexBytes[0], Arrays.copyOfRange(hexBytes, 1, hexBytes.length));
            } catch (Exception e) {
                log.error("tron转地址失败 =====> address: " + ethAddress, e);
            }
        }
        return null;
    }

    // 验签1开头的地址
    public static String btcSignedToOrdinaryAddress(String sign, String msg) {
        try {
            ECKey result = ECKey.signedMessageToKey(msg, sign);
            byte[] pubKeyHash = result.getPubKeyHash();
            return Base58.encodeChecked(0, pubKeyHash);
        } catch (Exception e) {
            log.error("btc验签失败 =====> sign:" + sign + "  =======> msg:" + msg, e);
            return null;
        }
    }

    // 验签3开头的地址
    public static String btcSignedToCompatibleAddress(String sign, String msg) {
        try {
            ECKey result = ECKey.signedMessageToKey(msg, sign);
            byte[] pubKeyHash = result.getPubKeyHash();
            return Base58.encodeChecked(5, Utils.sha256hash160(ByteUtils.concatAll(new byte[]{0x00, 0x14}, pubKeyHash)));
        } catch (Exception e) {
            log.error("btc验签失败 =====> sign:" + sign + "  =======> msg:" + msg, e);
            return null;
        }
    }

    // 验签bc开头的地址
    public static String btcSignedToPrimordialAddress(String sign, String msg) {
        try {
            ECKey result = ECKey.signedMessageToKey(msg, sign);
            byte[] pubKeyHash = result.getPubKeyHash();
            return Bech32.SegwitAddrEncode("bc", 0, pubKeyHash);
        } catch (Exception e) {
            log.error("btc验签失败 =====> sign:" + sign + "  =======> msg:" + msg, e);
            return null;
        }
    }

    // 验签之前先校验公钥对应的地址是否正确
    public static boolean verifyCardano(String sign, String msg, String publicKey) {
        try {
            EdDSANamedCurveSpec spec = EdDSANamedCurveTable.ED_25519_CURVE_SPEC;
            EdDSAEngine ver = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
            ver.initVerify(new EdDSAPublicKey(new EdDSAPublicKeySpec(Numeric.hexStringToByteArray(publicKey), spec)));
            ver.update(msg.getBytes(StandardCharsets.UTF_8));
            return ver.verify(Numeric.hexStringToByteArray(sign));
        } catch (Exception e) {
            log.error("cardano验签失败 =====> sign:" + sign + "  =======> msg:" + msg + "  =======> pubKey:" + publicKey, e);
            return false;
        }
    }

    public static boolean verifySolana(String sign, String msg, String address) {
        try {
            byte[] publickey = Base58.decode(address);
            TweetNaclFast.Signature signature = new TweetNaclFast.Signature(publickey, null);
            return signature.detached_verify(Base58.decode(msg), Base58.decode(sign));
        } catch (Exception e) {
            log.error("sol验签失败 =====> sign:" + sign + "  =======> msg:" + msg + "  =======> address:" + address, e);
            return false;
        }
    }

    public static boolean verifyApt(String sign, String msg, String address) {
        try {
            if(StringUtils.isBlank(sign) || StringUtils.isBlank(msg) || StringUtils.isBlank(address)) {
                return false;
            }
            if(sign.startsWith("0x")) {
                sign = sign.substring(2);
            }
            if(address.startsWith("0x")) {
                address = address.substring(2);
            }
            TweetNaclFast.Signature signature = new TweetNaclFast.Signature(Hex.decode(address), null);
            byte[] open = signature.open(Hex.decode(sign));
            return msg.equals(new String(open, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("apt验签失败 =====> sign:" + sign + "  =======> msg:" + msg + "  =======> address:" + address, e);
            return false;
        }
    }

    public static String pubKeyToPolkaAddress(String pubKey) {
        return SS58Codec.getInstance().encode(SS58Type.Network.LIVE, Numeric.hexStringToByteArray(pubKey));
    }

    // polka链 验签前先校验地址
    public static boolean verifySchnorrkel(String sign, String msg, String publicKey) {
        try {
            SigningContext ctx2 = SigningContext.createSigningContext(SIGNING_CONTEXT);
            SigningTranscript t2 = ctx2.bytes(msg.getBytes());
            KeyPair fromPublicKey = KeyPair.fromPublicKey(Numeric.hexStringToByteArray(publicKey));
            return fromPublicKey.verify(t2, Numeric.hexStringToByteArray(sign));
        } catch (Exception e) {
            log.error("polka验签失败 =====> sign:" + sign + "  =======> msg:" + msg + "  =======> pubKey:" + publicKey, e);
            return false;
        }
    }

    public static String filSignedToAddress(String sign, String msg) {
        try {
            byte[] cidHash = getCidHash(msg.getBytes(StandardCharsets.UTF_8));
            com.tianli.tool.crypto.fil.ECKey ecKey = com.tianli.tool.crypto.fil.ECKey.signatureToKey(cidHash, sign);
            byte[] pub = ecKey.getPubKey();
            Blake2b.Digest digest = Blake2b.Digest.newInstance(20);
            String hash = Hex.toHexString(digest.digest(pub));
            String pubKeyHash = "01" + Hex.toHexString(digest.digest(pub));
            Blake2b.Digest blake2b3 = Blake2b.Digest.newInstance(4);
            String checksum = Hex.toHexString(blake2b3.digest(Numeric.hexStringToByteArray(pubKeyHash)));
            return "f1" + cn.hutool.core.codec.Base32.encode(Numeric.hexStringToByteArray(hash + checksum)).toLowerCase();
        } catch (Exception e) {
            log.error("fil验签失败 =====> sign:" + sign + "  =======> msg:" + msg, e);
            return null;
        }
    }

    public static byte[] getCidHash(byte[] message) {
        Blake2b.Param param = new Blake2b.Param();
        param.setDigestLength(32);
        byte[] messageByte = Blake2b.Digest.newInstance(param).digest(message);

        int xlen = CID_PREFIX.length;
        int ylen = messageByte.length;

        byte[] result = new byte[xlen + ylen];

        System.arraycopy(CID_PREFIX, 0, result, 0, xlen);
        System.arraycopy(messageByte, 0, result, xlen, ylen);

        return Blake2b.Digest.newInstance(param).digest(result);
    }
}
