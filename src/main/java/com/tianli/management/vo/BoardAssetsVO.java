package com.tianli.management.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-31
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardAssetsVO {

    private String createTime;

    /**
     * 总资产
     */
    @BigDecimalFormat("0.00")
    private BigDecimal totalAssets = BigDecimal.ZERO;

    /**
     * 累计充值
     */
    @BigDecimalFormat("0.00")
    private BigDecimal accrueRechargeFee = BigDecimal.ZERO;

    /**
     * 累计提现
     */
    @BigDecimalFormat
    private BigDecimal accrueWithdrawFee = BigDecimal.ZERO;

    private List<BoardAssetsVO> data;

    public BoardAssetsVO(String createTime) {
        this.createTime = createTime;
    }
}
