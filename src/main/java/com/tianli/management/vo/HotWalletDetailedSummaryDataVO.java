package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class HotWalletDetailedSummaryDataVO {

    private BigDecimal rechargeAmountDollar;

    private BigDecimal withdrawAmountDollar;

    private BigDecimal userWithdrawAmountDollar;

    private BigDecimal imputationAmountDollar;
}
