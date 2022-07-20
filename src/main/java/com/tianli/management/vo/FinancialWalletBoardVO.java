package com.tianli.management.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
public class FinancialWalletBoardVO {

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

    private LocalDateTime createTime;
}
