package com.tianli.loan.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.enums.LoanStatusEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/5/26 14:34
 */
@Data
public class LoanListVo {

    private Long id;

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
     * 预估利息
     */
    private BigDecimal estimate_interest;

    /**
     * 滞纳金
     */
    private BigDecimal forfeit_penalty;

    /**
     * 当前使用天数
     */
    private Integer current_day;

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
     * 剩余应还金额
     */
    private BigDecimal remaining_amount_due;

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

    public static LoanListVo convert(Loan loan) {
        LoanListVo loanListVo = BeanUtil.copyProperties(loan, LoanListVo.class);
        BigDecimal amount = ObjectUtil.isNull(loan.getActual_amount()) ? loan.getExpect_amount() : loan.getActual_amount();
        loanListVo.setEstimate_interest(amount.multiply(loan.getRate()).multiply(Convert.toBigDecimal(loan.getRepayment_cycle())));
        if (ObjectUtil.isNotNull(loan.getActual_amount()) && loan.getActual_amount().compareTo(BigDecimal.ZERO) > 0) {
            loanListVo.setRemaining_amount_due((loan.getActual_amount().add(loan.getInterest()).add(loan.getForfeit_penalty())).subtract(loan.getRepayment_amount()));
        }
        return loanListVo;
    }
}
