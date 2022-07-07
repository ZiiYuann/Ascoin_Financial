package com.tianli.wallet.entity;

import com.tianli.currency.CurrencyTypeEnum;

import java.math.BigInteger;

/**
 * @author chenb
 * @apiNote 云钱包
 * @since 2022-07-06
 **/
public class Wallet {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 余额类型
     */
    private CurrencyTypeEnum type;

    /**
     * 总余额
     */
    private BigInteger balance;

    /**
     * 冻结余额
     */
    private BigInteger freeze;

    /**
     * 剩余余额
     */
    private BigInteger remain;

}
