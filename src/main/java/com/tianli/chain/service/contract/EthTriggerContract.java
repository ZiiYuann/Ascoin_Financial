package com.tianli.chain.service.contract;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.chain.dto.EthGasAPIResponse;
import com.tianli.common.ConfigConstants;
import com.tianli.common.HttpUtils;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.JsonRpc2_0Web3j;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class EthTriggerContract extends Web3jContractOperation {

    @Resource
    private ConfigService configService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private JsonRpc2_0Web3j ethWeb3j;

    @Override
    public String computeAddress(long uid) throws IOException {
        return computeAddress(new BigInteger("" + uid));
    }

    public String computeAddress(BigInteger uid) throws IOException {
        String address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        return computeAddress(address, uid);
    }

    public String computeAddress(String walletAddress, BigInteger uid) throws IOException {
        String contractAddress = configService.get(ConfigConstants.ETH_TRIGGER_ADDRESS);
        return super.computeAddress(walletAddress,uid,contractAddress);
    }

    public String recycle(String toAddress, CurrencyAdaptType currencyAdaptType, List<Long> addressId, List<String> erc20AddressList) {
        return super.recycle(toAddress,currencyAdaptType
                , configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT_PLUS, "800000")
                ,addressId,erc20AddressList);
    }

    @Override
    public Result transfer(String to, BigInteger val, CurrencyCoin coin) {
        String gasLimit = configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT, "200000");
        CurrencyAdaptType currencyAdaptType = CurrencyAdaptType.get(coin, NetworkType.erc20);
        return super.tokenTransfer(to,val,currencyAdaptType,gasLimit);
    }

    @Override
    protected JsonRpc2_0Web3j getWeb3j() {
        return ethWeb3j;
    }

    @Override
    public String getGas() {
        EthGasAPIResponse response = ethGas();
        if(Objects.isNull(response)){
            return configService.getOrDefault(ConfigConstants.ETH_GAS_PRICE,"80");
        }
        Double fast = response.getFast();
        if(Objects.isNull(fast)){
            return configService.getOrDefault(ConfigConstants.ETH_GAS_PRICE,"80");
        }
        return String.valueOf(fast);
    }

    @Override
    protected String getMainWalletAddress() {
        return configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
    }

    @Override
    protected String getMainWalletPassword() {
        return configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
    }

    @Override
    protected Long getChainId() {
        return Long.parseLong(configService.getOrDefault(ConfigConstants.ETH_CHAIN_ID, "1"));
    }

    /**
     * http调用ethScan获取gas
     */
    private EthGasAPIResponse ethGas() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("eth_gas");
        EthGasAPIResponse response = (EthGasAPIResponse) ops.get();
        if (response != null) return response;
        String stringResult = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet("https://api.etherscan.io/api?module=gastracker&action=gasoracle", "", "", Map.of(), Map.of());
            stringResult = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception ignore) {
            log.error("api.etherscan.io http掉用异常");
        }
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double safeGasPrice = JsonObjectTool.getAsDouble(jsonObject, "result.SafeGasPrice");
        Double proposeGasPrice = JsonObjectTool.getAsDouble(jsonObject, "result.ProposeGasPrice");
        Double fastGasPrice = JsonObjectTool.getAsDouble(jsonObject, "result.FastGasPrice");
        if (safeGasPrice != null && proposeGasPrice != null && fastGasPrice != null) {
            response = new EthGasAPIResponse();
            response.setFastest(fastGasPrice);
            response.setFast(proposeGasPrice);
            response.setAverage(safeGasPrice);
            response.setSafeLow(safeGasPrice);
        }
        if (response == null || response.getFast() == null) {
            return null;
        }
        ops.set(response, 1L, TimeUnit.MINUTES);
        return response;
    }
}
