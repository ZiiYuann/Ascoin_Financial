package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
@Data
@Builder
public class FinancialProductBoardSummaryVO {

    // 申购
    private BigDecimal purchaseAmount;
    // 赎回
    private BigDecimal redeemAmount;
    // 结算
    private BigDecimal settleAmount;
    //转存
    private BigDecimal  transferAmount;

    // 详情
    private List<FinancialProductBoardVO> data;

}
