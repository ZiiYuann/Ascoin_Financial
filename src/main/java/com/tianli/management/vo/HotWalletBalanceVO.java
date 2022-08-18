package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotWalletBalanceVO {

    private BigDecimal usdtBep20;
    private BigDecimal usdcBep20;
    private BigDecimal bnb;

    private BigDecimal usdtERC20;
    private BigDecimal usdcERC20;
    private BigDecimal eth;

    private BigDecimal usdtTRC20;
    private BigDecimal usdcTRC20;
    private BigDecimal trx;
}
