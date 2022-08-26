package com.tianli.chain.service.contract;

import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.Result;

import java.io.IOException;
import java.math.BigDecimal;
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
     * @param tokens 代币tokens 链需要合约token
     * @return 返回交易hash
     */
    String recycle(String toAddress, List<Long> addressIds,List<String> tokens);

    /**
     * 转账
     * @param to 发送地址
     * @param val 转账数额
     * @param tokenAdapter 代币包装
     * @return 交易结果
     */
    Result transfer(String to, BigInteger val, TokenAdapter tokenAdapter);

    /**
     * 校验地址是否有效
     * @param address 地址
     * @return 是否有效
     */
    boolean isValidAddress(String address);

    /**
     * 根据hash获取交易状态
     * @param hash 交易hash
     * @return 交易状态
     */
    boolean successByHash(String hash);


    /**
     * 主币余额
     * @param address 钱包地址
     * @return 余额
     */
    BigDecimal mainBalance(String address);

    /**
     * 代币余额
     * @param address 钱包地址
     * @param tokenAdapter 代币合约
     * @return 余额
     */
    BigDecimal tokenBalance(String address,TokenAdapter tokenAdapter);
}
