package com.tianli.common.blockchain;

import com.tianli.charge.ChargeService;
import com.tianli.common.ConfigConstants;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UsdtEthContract {

    @Resource
    private EthBlockChainActuator ethBlockChainActuator;

    @Resource
    private ConfigService configService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private RedisLock redisLock;

    public Result transfer(String to, String val) {
        BigInteger bigInteger = new BigDecimal(val).movePointRight(6).toBigInteger();
        return transfer(to, bigInteger);
    }

    public Result transfer(String to, BigInteger val) {
        String address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        long nonce = ethBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            String BFContractAddress = configService.get(ConfigConstants.ETH_USDT_ADDRESS);;
            result = ethBlockChainActuator.tokenSendRawTransaction(nonce,
                    BFContractAddress,
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    ethBlockChainActuator.getPrice(),
                    configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT,"200000"),
                    password, "转账USDT: ");
        } catch (Exception ignored) {
        }
        return result;
    }

    public Result transferUsdc(String to, BigInteger val) {
        String address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        long nonce = ethBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            String BFContractAddress = configService.get(ConfigConstants.ETH_USDC_ADDRESS);;
            result = ethBlockChainActuator.tokenSendRawTransaction(nonce,
                    BFContractAddress,
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    ethBlockChainActuator.getPrice(),
                    configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT,"200000"),
                    password, "转账USDT: ");
        } catch (Exception ignored) {
        }
        return result;
    }

    public Result transferEth(String to, BigInteger val) {
        String address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        long nonce = ethBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            result = ethBlockChainActuator.mainTokenSendRawTransaction(nonce,
                    to,
                    val,
                    ethBlockChainActuator.getPrice(),
                    configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT,"200000"),
                    password, "转账ETH: ");
        } catch (Exception ignored) {
        }
        return result;
    }

    public Result transferToken(String to, BigInteger val, CurrencyCoinEnum token) {
        String address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        long nonce = ethBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            result = ethBlockChainActuator.tokenSendRawTransaction(nonce,
                    "tokenContract.getContract_address()",
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    ethBlockChainActuator.getPrice(),
                    configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT,"200000"),
                    password, "转账token: ");
        } catch (Exception ignored) {
        }
        return result;
    }

}
