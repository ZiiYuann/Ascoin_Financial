package com.tianli.loan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 贷款周期表
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("loan_cycle")
public class LoanCycle extends Model<LoanCycle> {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 还款周期
     */
    @NotNull
    private Integer repayment_cycle;

    /**
     * 日利率
     */
    @NotNull
    private BigDecimal day_rate;

    /**
     * 滞纳金利率
     */
    @NotNull
    private BigDecimal late_fee_rate;

    /**
     * 创建日期
     */
    private LocalDateTime create_time;

    private LocalDateTime update_time;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
