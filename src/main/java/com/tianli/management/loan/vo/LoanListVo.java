package com.tianli.management.loan.vo;

import com.tianli.loan.enums.LoanStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/26 15:28
 */
@Data
public class LoanListVo {

    private Long id;

    private String username;

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

    private Long create_time_ms;

    private LocalDateTime create_time;

    /**
     * 还款时间
     */
    private Long repayment_time_ms;

    /**
     * 还款金额
     */
    private BigDecimal repayment_amount;

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
}
