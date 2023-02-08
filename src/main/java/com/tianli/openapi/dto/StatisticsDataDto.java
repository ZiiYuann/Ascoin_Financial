package com.tianli.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-05
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDataDto {

    private BigDecimal balance;

    private BigDecimal rechargeAmount;

    private BigDecimal withdrawAmount;

    private BigDecimal purchaseAmount;

    private BigDecimal redeemAmount;

    private BigDecimal incomeAmount;

    private BigDecimal subBalance;

    private BigDecimal subPurchaseAmount;

    private BigDecimal subRedeemAmount;

    private Long chatId;

    private Long uid;

}
