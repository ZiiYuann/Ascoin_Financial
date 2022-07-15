package com.tianli.common.blockchain;

import com.tianli.charge.service.ChargeService;
import com.tianli.common.ConfigConstants;
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
public class UsdtBscContract {

    @Resource
    private BscBlockChainActuator bscBlockChainActuator;

    @Resource
    private ConfigService configService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private RedisLock redisLock;

    public Result transfer(String to, String val) {
        var bigInteger = new BigDecimal(val).movePointRight(18);
        return transfer(to, bigInteger);
    }

    public Result transfer(String to, BigDecimal val) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            String BFContractAddress = configService.get(ConfigConstants.BSC_USDT_ADDRESS);;
            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    BFContractAddress,
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val.toBigInteger())), new ArrayList<>())
                    ),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000"),
                    password, "转账USDT: ");
        } catch (Exception ignored) {
        }
        return result;
    }

    public Result transferUsdc(String to, BigInteger val) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            String BFContractAddress = configService.get(ConfigConstants.BSC_USDC_ADDRESS);;
            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    BFContractAddress,
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000"),
                    password, "转账USDC: ");
        } catch (Exception ignored) {
        }
        return result;
    }

    public Result transferToken(String to, BigInteger val, CurrencyCoin token) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可

            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    "tokenContract.getContract_address()",
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000"),
                    password, "转账token: ");
        } catch (Exception ignored) {
        }
        return result;
    }

    public Result transferBNB(String to, BigInteger val) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            result = bscBlockChainActuator.mainTokenSendRawTransaction(
                    nonce,
                    to,
                    val,
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000"),
                    password, "转账token: ");
        } catch (Exception ignored) {
        }
        return result;
    }
}
