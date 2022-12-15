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
}
