package com.tianli.chain.service.contract;

import cn.hutool.json.JSONUtil;
import com.tianli.chain.enums.ChainType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.JsonRpc2_0Web3j;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-26
 **/
@Slf4j
//@Component
public class TronWeb3jContract {

    @Resource
    private JsonRpc2_0Web3j tronWeb3j;

    public String computeAddress(long addressId) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String computeAddress(BigInteger addressId) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String computeAddress(String walletAddress, BigInteger addressId) throws IOException {
        throw new UnsupportedOperationException();
    }

//    @Override
//    protected JsonRpc2_0Web3j getWeb3j() {
//        return tronWeb3j;
//    }
//
//    @Override
//    public String getGas() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected String getMainWalletAddress() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected String getMainWalletPassword() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected Long getChainId() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected String getRecycleGasLimit() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected String getTransferGasLimit() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected String getRecycleTriggerAddress() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public BigDecimal getConsumeFee(String hash) throws IOException {
//
//        HttpClient client = HttpClientBuilder.create().build();
//        HttpGet httpGet = new HttpGet("https://apilist.tronscanapi.com/api/transaction-info?hash=" + hash);
//
//        HttpResponse execute = client.execute(httpGet);
//        String result = EntityUtils.toString(execute.getEntity());
//
//        BigDecimal energyFee = JSONUtil.parseObj(result).getByPath("cost.energy_fee", BigDecimal.class);
//        BigDecimal netFee = JSONUtil.parseObj(result).getByPath("cost.net_fee", BigDecimal.class);
//
//        return energyFee.add(netFee).multiply(BigDecimal.valueOf(0.000001));
//
//    }
}
