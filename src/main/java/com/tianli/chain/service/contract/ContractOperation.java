package com.tianli.chain.service.contract;

import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.TransactionStatus;
import com.tianli.common.blockchain.NetworkType;

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

    String computeAddress(long addressId) throws IOException;

    String computeAddress(BigInteger addressId) throws IOException;

    String computeAddress(String walletAddress, BigInteger addressId) throws IOException;

    /**
     * 归集接口
     *
     * @param toAddress  归集地址 如果为null，修改为主钱包地址
     * @param addressIds Address表中的id
     * @param tokens     代币tokens 链需要合约token
     * @return 返回交易hash
     */
    String recycle(String toAddress, List<Long> addressIds, List<String> tokens);

    /**
     * 转账
     *
     * @param to   发送地址
     * @param val  转账数额
     * @param coin 代币信息
     * @return 交易结果
     */
    String transfer(String to, BigInteger val, Coin coin);

    /**
     * 校验地址是否有效
     *
     * @param address 地址
     * @return 是否有效
     */
    boolean isValidAddress(String address);

    /**
     * 根据hash获取交易状态
     *
     * @param hash 交易hash
     * @return 交易状态
     */
    TransactionStatus successByHash(String hash);


    /**
     * 主币余额
     *
     * @param address 钱包地址
     * @return 余额
     */
    BigDecimal mainBalance(String address);

    /**
     * 代币余额
     *
     * @param address 钱包地址
     * @param coin    代币信息
     * @return 余额
     */
    BigDecimal tokenBalance(String address, Coin coin);

    /**
     * 获取合约的小数点位数
     *
     * @param contractAddress 合约地址
     * @return 小数点位数
     */
    Integer decimals(String contractAddress);

    BigDecimal getConsumeFee(String hash) throws IOException;

    /**
     * @param chain todo 类型需要修改
     * @return boolean
     */
    boolean matchByChain(NetworkType chain);
}
