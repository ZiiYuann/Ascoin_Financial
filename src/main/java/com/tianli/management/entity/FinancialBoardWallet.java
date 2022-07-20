package com.tianli.management.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
public class FinancialBoardWallet {

    private Long id;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;

    /**
     * 提币金额
     */
    private BigDecimal withdrawAmount;

    /**
     * 激活云钱包数量
     */
    private BigInteger activeWalletCount;

    /**
     * 创建时间
     */
    private LocalDate createTime;

    /**
     * 总手续费
     */
    private BigDecimal totalServiceAmount;

    /**
     * usdt手续费
     */
    private BigDecimal usdtServiceAmount;

}
