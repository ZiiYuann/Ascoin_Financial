package com.tianli.newcurrency;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 每天0点用户币余额
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@ApiModel(value="NewCurrencyDay对象", description="每天0点用户币余额")
public class NewCurrencyDayDTO extends Model<NewCurrencyDayDTO> {

    private static final long serialVersionUID=1L;

    //总锁仓资金:用户投入的
    private BigDecimal inputAmount;
    //总募集资金：用户实际扣除
    private BigDecimal inputAmountReal;
    //发行项目数
    private Long projectCount;
    //参与总人数
    private Long personCount;
}
