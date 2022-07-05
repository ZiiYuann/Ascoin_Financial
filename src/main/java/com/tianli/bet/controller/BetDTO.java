package com.tianli.bet.controller;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 押注表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BetDTO implements Serializable {

    /**
     * 押注类型
     */
    @NotNull(message = "押注类型不能为空")
    private BetTypeEnum betType;

    /**
     * 竞猜时间,单位:分钟
     */
    @NotNull(message = "押注时间不能为空")
    private Double betTime;

    /**
     * 押注金额
     */
    @NotNull(message = "押注金额不能为空")
    @DecimalMin(value = "0.0000000001", message = "押注金额必须大于0")
    private BigDecimal amount;

    /**
     * 押注方向
     */
    @NotNull(message = "押注方向不能为空")
    private KlineDirectionEnum betDirection;

    /**
     * 押注币种交易对
     */
    private String betSymbol;
}
