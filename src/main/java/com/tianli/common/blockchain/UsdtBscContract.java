package com.tianli.common.blockchain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.Constants;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.Result;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
        BigInteger bigInteger = new BigDecimal(val).movePointRight(18).toBigInteger();
        return transfer(to, bigInteger);
    }

    public Result transfer(String to, BigInteger val) {
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
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
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

    public Result transferToken(String to, BigInteger val, CurrencyCoinEnum token) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        Result result = null;
        try {
            //后续如果需要其他的币种转账  修改这个合约地址即可
            TokenContract tokenContract = tokenContractService.getOne(new LambdaQueryWrapper<TokenContract>()
                    .eq(TokenContract::getChain, ChainType.bep20).eq(TokenContract::getToken, token)
            );
            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    tokenContract.getContract_address(),
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


//    @Scheduled(fixedDelay = 10_000)
//    public void checkTransferResult(){
//        CompletableFuture.runAsync(() -> {
//            String lockStr = "BSC-checkTransferResult-KEY";
//            boolean lock = redisLock._lock(lockStr, 1L, TimeUnit.MINUTES);
//            if(!lock){
//                return;
//            }
//            try {
//                List<Charge> list = chargeService.list(Wrappers.<Charge>lambdaQuery()
//                        .in(Charge::getCurrency_type, List.of(TokenCurrencyType.usdt_bep20, TokenCurrencyType.BF_bep20, TokenCurrencyType.usdc_bep20))
//                        .eq(Charge::getStatus, ChargeStatus.chaining)
//                        .eq(Charge::getCharge_type, ChargeType.withdraw)
//                        .isNotNull(Charge::getTxid)
//                );
//                // todo 查询txid
//                list.parallelStream().forEach(e -> {
//                    TransactionResult transactionResult = bscBlockChainActuator.roughJudgmentTransactionByHash(e.getTxid());
//                    Boolean success;
//                    if(Objects.isNull(transactionResult) || Objects.isNull(success = transactionResult.getSuccess())){
//                        return;
//                    }
//                    if (success) {
//                        chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
//                                .status("success")
//                                .txid(e.getTxid())
//                                .sn(e.getSn())
//                                .miner_fee_type(TokenCurrencyType.bnb)
//                                .miner_fee(transactionResult.getFee()).build());
////                        chargeService.update(Wrappers.lambdaUpdate(Charge.class)
////                                .eq(Charge::getId, e.getId())
////                                .set(Charge::getStatus, ChargeStatus.chain_success)
////                                .set(Charge::getComplete_time, LocalDateTime.now())
////                                .set(Charge::getFee, transactionResult.getFee())
////                        );
//                    } else {
//                        chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
//                                .status("fail")
//                                .txid(e.getTxid())
//                                .sn(e.getSn())
//                                .miner_fee_type(TokenCurrencyType.bnb)
//                                .miner_fee(transactionResult.getFee()).build());
////                        chargeService.update(Wrappers.lambdaUpdate(Charge.class)
////                                .eq(Charge::getId, e.getId())
////                                .set(Charge::getStatus, ChargeStatus.chain_fail)
////                                .set(Charge::getComplete_time, LocalDateTime.now())
////                                .set(Charge::getFee, transactionResult.getFee())
////                        );
//                    }
//                });
//            }catch (Exception ignore){}finally {
//                redisLock.unlock(lockStr);
//            }
//        }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
//    }
    @Resource
    private TokenContractService tokenContractService;

}
