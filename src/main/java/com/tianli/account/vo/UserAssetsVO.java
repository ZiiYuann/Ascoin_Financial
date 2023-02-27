package com.tianli.account.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-08
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAssetsVO {

    private Long uid;

    @BigDecimalFormat("0.00")
    private BigDecimal assets;

    @BigDecimalFormat("0.00")
    private BigDecimal financialHoldAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal fundHoldAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal purchaseAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal balanceAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal freezeAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal pledgeFreezeAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal remainAmount;

    public static UserAssetsVO defaultInstance() {
        UserAssetsVO userAssetsVO = new UserAssetsVO();
        userAssetsVO.setAssets(BigDecimal.ZERO);
        userAssetsVO.setFinancialHoldAmount(BigDecimal.ZERO);
        userAssetsVO.setFundHoldAmount(BigDecimal.ZERO);
        userAssetsVO.setPurchaseAmount(BigDecimal.ZERO);
        userAssetsVO.setBalanceAmount(BigDecimal.ZERO);
        userAssetsVO.setFreezeAmount(BigDecimal.ZERO);
        userAssetsVO.setRemainAmount(BigDecimal.ZERO);
        return userAssetsVO;
    }
}
