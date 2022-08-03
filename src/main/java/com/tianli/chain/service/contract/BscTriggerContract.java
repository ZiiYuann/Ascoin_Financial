package com.tianli.chain.service.contract;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.common.ConfigConstants;
import com.tianli.common.HttpUtils;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.JsonRpc2_0Web3j;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BscTriggerContract extends Web3jContractOperation {

    @Resource
    private ConfigService configService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private JsonRpc2_0Web3j bscWeb3j;

    @Override
    public String computeAddress(long uid) throws IOException {
        return computeAddress(new BigInteger("" + uid));
    }

    public String computeAddress(BigInteger uid) throws IOException {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        return computeAddress(address, uid);
    }

    public String computeAddress(String walletAddress, BigInteger uid) throws IOException {
        String contractAddress = configService.get(ConfigConstants.BSC_TRIGGER_ADDRESS);
        return super.computeAddress(walletAddress,uid,contractAddress);
    }

    public String recycle(String toAddress,CurrencyAdaptType currencyAdaptType, List<Long> addressId, List<String> bep20AddressList) {
        return super.recycle(toAddress,currencyAdaptType
                , configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT_PLUS,"10000000")
                ,addressId,bep20AddressList);
    }

    @Override
    public Result transfer(String to, BigInteger val, CurrencyCoin coin) {
        String gasLimit = configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT, "800000");
        CurrencyAdaptType currencyAdaptType = CurrencyAdaptType.get(coin, NetworkType.bep20);
        return super.tokenTransfer(to,val,currencyAdaptType,gasLimit);
    }

    @Override
    protected JsonRpc2_0Web3j getWeb3j() {
        return bscWeb3j;
    }

    @Override
    protected String getGas() {
        return configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5");
    }

    @Override
    protected String getMainWalletAddress() {
        return configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
    }

    @Override
    protected String getMainWalletPassword() {
        return configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
    }

    @Override
    protected Long getChainId() {
        return Long.parseLong(configService.getOrDefault(ConfigConstants.BSC_CHAIN_ID, "56"));
    }
}
