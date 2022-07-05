package com.tianli.management.platformfinance;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2021-01-12 16:42
 * @since 1.0.0
 */
@Data
public class FinanceExhibitionDetailDTO {
    private LocalDate date;

    /**
     * 用户余额提现手续费
     */
    private BigInteger withdrawal_fee_erc20;
    private BigInteger withdrawal_fee_omni;

    /**
     * 代理商分红结算手续费
     */
    private BigInteger settlement_erc20_fee;
    private BigInteger settlement_omni_fee;

    /**
     * 代理商撤回保证金手续费
     */
    private BigInteger deposit_erc20_fee;
    private BigInteger deposit_omni_fee;
}
