package com.tianli.common.blockchain;

import com.tianli.exception.Result;
import com.tianli.management.ruleconfig.ConfigConstants;
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
public class BFBscContract {

    @Resource
    private BscBlockChainActuator bscBlockChainActuator;

    @Resource
    private ConfigService configService;

    public Result transfer(String to, String val) {
        BigInteger bigInteger = new BigDecimal(val).movePointRight(18).toBigInteger();
        return transfer(to, bigInteger);
    }

    public Result transfer(String to, BigInteger val) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            String bFContractAddress = configService.get(ConfigConstants.BSC_BF_ADDRESS);;
            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    bFContractAddress,
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000"),
                    password, "转账BF: ");
        } catch (Exception ignored) {
        }
        return result;
    }
}
