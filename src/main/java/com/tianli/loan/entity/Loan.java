package com.tianli.loan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.loan.enums.LoanStatusEnum;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("loan")
public class Loan extends Model<Loan> {

    private static final long serialVersionUID=1L;

    private Long id;

    private Long uid;

    /**
     * 期望借款金额
     */
    private BigDecimal expect_amount;

    /**
     * 实际借款金额
     */
    private BigDecimal actual_amount;

    /**
     * 借款的币种
     */
    private String token;

    /**
     * 日利率
     */
    private BigDecimal rate;
    /**
     * 滞纳金利率
     */
    private BigDecimal late_fee_rate;

    /**
     * 还款周期
     */
    private Integer repayment_cycle;

    /**
     * 还款方式 
     */
    private String repayment;

    /**
     * 借款状态
     */
    private LoanStatusEnum status;

    /**
     * 利息
     */
    private BigDecimal interest;

    /**
     * 滞纳金
     */
    private BigDecimal forfeit_penalty;

    /**
     * 当前使用天数
     */
    private Integer current_day;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    /**
     * 使用天数下次更新时间
     */
    private Long next_update_time;

    private LocalDateTime update_time;

    private Long create_time_ms;

    /**
     * 还款时间
     */
    private Long repayment_time_ms;

    /**
     * 还款金额
     */
    private BigDecimal repayment_amount;
    /**
     * 已还本金
     */
    private BigDecimal repayment_principal;

    /**
     * 已还款利息
     */
    private BigDecimal repayment_interest;

    /**
     * 已还滞纳金
     */
    private BigDecimal repayment_forfeit_penalty;

    /**
     * 审核时间
     */
    private Long review_time;

    /**
     * 审核原因
     */
    private String reason;

    /**
     * 审核原因英文
     */
    private String reason_en;

    /**
     * 审核人
     */
    private String reviewer;

    /**
     * 审核人id
     */
    private Long reviewer_id;

    private String image_1;

    private String image_2;

    private String image_3;

    private String image_4;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
