package com.tianli.account.entity;

import com.tianli.chain.ChainType;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.enums.CurrencyType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * @author chenb
 * @apiNote 账户余额
 * @since 2022-07-07
 **/
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AccountBalance {

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
     * 余额币种类型
     */
    private CurrencyType currencyType;

    /**
     * 链类型
     */
    private ChainType chainType;

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

    /**
     * 总余额
     */
    private BigInteger balanceBF;

    /**
     * 冻结余额
     */
    private BigInteger freezeBF;

    /**
     * 剩余余额
     */
    private BigInteger remainBF;
}
