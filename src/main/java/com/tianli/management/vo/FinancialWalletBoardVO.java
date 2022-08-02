package com.tianli.management.vo;

import lombok.Builder;
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
@Builder
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

    /**
     * 总手续费
     */
    private BigDecimal totalServiceAmount;

    /**
     * usdt手续费
     */
    private BigDecimal usdtServiceAmount;

    private LocalDate createTime;

    public static FinancialWalletBoardVO getDefault(LocalDate createTime) {
        return FinancialWalletBoardVO.builder()
                .rechargeAmount(BigDecimal.ZERO)
                .withdrawAmount(BigDecimal.ZERO)
                .activeWalletCount(BigInteger.ZERO)
                .totalServiceAmount(BigDecimal.ZERO)
                .usdtServiceAmount(BigDecimal.ZERO)
                .createTime(createTime).build();
    }
}
