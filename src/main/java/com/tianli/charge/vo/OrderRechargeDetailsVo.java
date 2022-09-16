package com.tianli.charge.vo;

import com.tianli.management.vo.ProductLadderRateVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderRechargeDetailsVo extends OrderBaseVO {

    /**
     * 申购时间
     */
    private LocalDateTime purchaseTime;

    /**
     * 预估收益
     */
    private BigDecimal expectIncome;

    private byte rateType;

    /**
     * 阶梯化利率
     */
    private List<ProductLadderRateVO> ladderRates;

}
