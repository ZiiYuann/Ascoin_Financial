package com.tianli.management.vo;

import com.tianli.charge.enums.ChargeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
@Data
@Builder
public class FinancialProductBoardVO {

    // 申购
    private BigDecimal purchaseAmount;
    // 赎回
    private BigDecimal redeemAmount;
    // 结算
    private BigDecimal settleAmount;
    //转存
    private BigDecimal  transferAmount;

    // 详情
    private Map<ChargeType, List<FinancialBoardDataVO>> details;

}
