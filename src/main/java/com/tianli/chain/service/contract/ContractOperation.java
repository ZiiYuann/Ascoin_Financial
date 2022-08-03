package com.tianli.chain.service.contract;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
public interface ContractOperation {

    String computeAddress(long uid) throws IOException;

    String computeAddress(BigInteger uid) throws IOException;

    String computeAddress(String walletAddress, BigInteger uid) throws IOException;

    /**
     * 归集接口
     * @param toAddress 归集地址 如果为null，修改为主钱包地址
     * @param addressIds Address表中的id
     * @param addresses 归集地址列表
     * @return 返回交易hash
     */
    String recycle(String toAddress, CurrencyAdaptType currencyAdaptType, List<Long> addressIds, List<String> addresses);

    /**
     * 转账
     * @param to 发送地址
     * @param coin 币别
     * @param val 转账数额
     * @return 交易结果
     */
    Result transfer(String to, BigInteger val, CurrencyCoin coin);

    /**
     * 校验地址是否有效
     * @param address 地址
     * @return 是否有效
     */
    boolean isValidAddress(String address);

}
