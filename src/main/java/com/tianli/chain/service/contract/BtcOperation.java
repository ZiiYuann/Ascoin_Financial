package com.tianli.chain.service.contract;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tianli.address.PushHttpClient;
import com.tianli.address.Service.AddressMnemonicService;
import com.tianli.chain.dto.RawTransaction;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.UutokenHttpService;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrCodeException;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Component;
import party.loveit.bip44forjava.core.ECKey;
import party.loveit.bip44forjava.utils.Bip44Utils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author cs
 * @Date 2022-12-26 10:19
 */
@Slf4j
@Component
public class BtcOperation extends AbstractContractOperation {

    @Value("${rpc.btc.url}")
    private String url;
    @Value("${rpc.btc.username}")
    private String username;
    @Value("${rpc.btc.password}")
    private String password;
    @Resource
    private ConfigService configService;
    @Resource
    private AddressMnemonicService addressMnemonicService;
    @Resource
    private UutokenHttpService uutokenHttpService;

    @Override
    Result tokenTransfer(String to, BigInteger val, Coin coin) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public String computeAddress(long addressId) throws IOException {
        String mnemonic = addressMnemonicService.getMnemonic(addressId);
        BigInteger prvBtc = Bip44Utils.getDefaultPathPrivateKey(Collections.singletonList(mnemonic), 0);
        ECKey ecKey = ECKey.fromPrivate(prvBtc);
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        return Base58.encodeChecked(0, pubKeyHash);
    }

    @Override
    public String recycle(String toAddress, List<Long> addressIds, List<String> tokenContractAddresses) {
        return null;
    }

    @Override
    Result mainTokenTransfer(String to, BigInteger val, Coin coin) {
        String fromAddress = configService.get(ConfigConstants.BTC_MAIN_WALLET_ADDRESS);
        String prvKey = configService.get(ConfigConstants.BTC_PRIVATE_KEY);
        return Result.success();
    }

    @Override
    public boolean successByHash(String hash) {
        return false;
    }

    @Override
    public BigDecimal mainBalance(String address) {
        return new BigDecimal(uutokenHttpService.btcBalance(address));
    }

    @Override
    public BigDecimal tokenBalance(String address, TokenAdapter tokenAdapter) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public Integer decimals(String contractAddress) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    public static void main(String[] args) throws IOException {
        BtcOperation btcOperation = new BtcOperation();
        btcOperation.url = "http://35.77.16.20:8332";
        btcOperation.username = "user";
        btcOperation.password = "hxTyOjmTh9bfjiivdzBlNH9HSWDZzf7UBM5PhbOMxHR";
        BigDecimal consumeFee = btcOperation.getConsumeFee("9aadded09a0cf478593037bf6f815e251c900796129c4c9821b0302e74b9bb2a");
    }

    @Override
    public BigDecimal getConsumeFee(String hash) throws IOException {
        RawTransaction rawTransaction = getrawtransaction(hash);
        BigDecimal btcFee = BigDecimal.ZERO;
        for (RawTransaction.Vin vin : rawTransaction.getVin()) {
            if(StringUtils.isNotBlank(vin.getTxid()) && vin.getVout() != null) {
                RawTransaction.Vout vout = getrawtransaction(vin.getTxid()).getVout().get(vin.getVout());
                btcFee = btcFee.add(new BigDecimal(vout.getValue()).movePointRight(8));
            }
        }
        for (RawTransaction.Vout vout : rawTransaction.getVout()) {
            btcFee = btcFee.subtract(new BigDecimal(vout.getValue()).movePointRight(8));
        }
        return btcFee;
    }

    @Override
    public boolean matchByChain(NetworkType chain) {
        return NetworkType.btc.equals(chain);
    }

    public String sendPrivateKey(String privateKeys, JsonArray array, JsonArray out, long fee,
                                 String firstAddress, String lastAddress,
                                 long vinValue, long voutValue1, long voutValue2) {
        if (!validAddress(firstAddress) || !validAddress(lastAddress) || firstAddress.equals(lastAddress) ||
                voutValue1 < 546L || voutValue2 < 0L) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        MainNetParams params = MainNetParams.get();
        long out_amount = voutValue1 + voutValue2;
        if (out_amount + fee > vinValue) throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privateKeys);
        org.bitcoinj.core.ECKey ecKey = dumpedPrivateKey.getKey();
        Transaction transaction = new Transaction(params);

        for (int i = 0; i < out.size(); i++) {
            JsonObject asJsonObject = out.get(i).getAsJsonObject();
            long value = asJsonObject.get("value").getAsLong();
            String address = asJsonObject.get("address").getAsString();
            transaction.addOutput(org.bitcoinj.core.Coin.valueOf(value), Address.fromString(params, address));
        }
        for (int i = 0; i < array.size(); i++) {
            JsonObject asJsonObject = array.get(i).getAsJsonObject();
            long vout = asJsonObject.get("vout").getAsLong();
            long value = asJsonObject.get("value").getAsLong();
            String hash = asJsonObject.get("txid").getAsString();
            Sha256Hash sha256Hash = Sha256Hash.wrap(hash);
            TransactionInput input = new TransactionInput(params, transaction, ScriptBuilder.createEmpty().getProgram(), new TransactionOutPoint(params, vout, sha256Hash), org.bitcoinj.core.Coin.valueOf(value));
            input.setSequenceNumber(TransactionInput.NO_SEQUENCE - 2);
            transaction.addInput(input);
        }

        for (int i = 0; i < array.size(); i++) {
            TransactionInput input = transaction.getInput(i);
            JsonObject asJsonObject = array.get(i).getAsJsonObject();
            String address = asJsonObject.get("address").getAsString();
            long value = asJsonObject.get("value").getAsLong();
            if (address.startsWith("1")) {
                Address fromString = Address.fromString(params, address);
                Script scriptPubKey = ScriptBuilder.createOutputScript(fromString);
                Sha256Hash sha256Hash1 = transaction.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
                org.bitcoinj.core.ECKey.ECDSASignature ecdsaSignature = ecKey.sign(sha256Hash1);
                TransactionSignature signature = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);
                input.setScriptSig(ScriptBuilder.createInputScript(signature, ecKey));
            } else if (address.startsWith("3")) {
                Script p2WPKHOutputScript = ScriptBuilder.createP2WPKHOutputScript(ecKey);
                Script witnessScript = ScriptBuilder.createP2PKHOutputScript(ecKey);
                TransactionSignature signature = transaction.calculateWitnessSignature(i, ecKey, witnessScript, org.bitcoinj.core.Coin.valueOf(value), Transaction.SigHash.ALL, false);
                input.setWitness(TransactionWitness.redeemP2WPKH(signature, ecKey));
                input.setScriptSig(new ScriptBuilder().data(p2WPKHOutputScript.getProgram()).build());
            } else if (address.startsWith("bc")) {
                Script witnessScript = ScriptBuilder.createP2PKHOutputScript(ecKey);
                TransactionSignature signature = transaction.calculateWitnessSignature(i, ecKey, witnessScript, org.bitcoinj.core.Coin.valueOf(value), Transaction.SigHash.ALL, false);
                input.setWitness(TransactionWitness.redeemP2WPKH(signature, ecKey));
            }
        }
        String hexString = Hex.toHexString(transaction.bitcoinSerialize());
        String sendrawtransaction = sendrawtransaction(hexString);
        if (StringUtils.isNotBlank(sendrawtransaction)) {
            return sendrawtransaction;
        }
        return null;
    }

    public RawTransaction getrawtransaction(String txid) {
        JSONObject jsonObject = httpJson("getrawtransaction", txid, 1);
        return jsonObject.get("result", RawTransaction.class);
    }

    public String sendrawtransaction(String hexstring) {
        JSONObject jsonObject = httpJson("sendrawtransaction", hexstring);
        String result;
        try {
            result = jsonObject.get("result", String.class);
        } catch (Exception e) {
            result = "";
        }
        if (StringUtils.isBlank(result)) {
            JSONObject error = jsonObject.getJSONObject("error");
            int code = error.get("code", Integer.class);
            if (code == -26) throw new  ErrCodeException("发送失败,请等待上一笔交易完成");
            throw new ErrCodeException("发送交易失败");
        }
        return result;
    }

    private JSONObject httpJson(String method, Object... params) {
        return JSONUtil.parseObj(http(method, params));
    }

    private String http(String method, Object... params) {
        try {
            long id = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
            HashMap<String, Object> map = new HashMap<>();
            map.put("method", method);
            map.put("id", String.valueOf(id));
            map.put("jsonrpc", "2.0");
            map.put("params", params);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(Utf8.encode(username + ":" + password)));
            var jsonStr = JSONUtil.parse(map);
            byte[] bytes = jsonStr.toString().getBytes(StandardCharsets.UTF_8);
            httpPost.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length));

            log.info("推送钱包地址监控信息为: 【{}】", jsonStr);
            HttpResponse response = PushHttpClient.getClient().execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("btc请求上链失败 method:{}  params:{}",method,  params, e);
            return "{}";
        }
    }
}
