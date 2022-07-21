package com.tianli.charge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

}
