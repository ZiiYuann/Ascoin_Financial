package com.tianli.account.vo;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
public class AccountBalanceVO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 币种
     */
    private CurrencyAdaptType currencyAdaptType;

    /**
     * 用户账户地址
     */
    private String address;

    /**
     * 转化成为美元的汇率
     */
    private BigDecimal dollarRate;

    /**
     * 总余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal freeze;

    /**
     * 剩余余额
     */
    private BigDecimal remain;

    /**
     * 总余额 美元
     */
    private BigDecimal dollarBalance;

    /**
     * 冻结余额 美元
     */
    private BigDecimal dollarFreeze;

    /**
     * 剩余余额 美元
     */
    private BigDecimal dollarRemain;

}
