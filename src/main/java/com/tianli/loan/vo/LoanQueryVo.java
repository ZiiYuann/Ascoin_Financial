package com.tianli.loan.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.enums.LoanStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/26 11:50
 */
@Data
public class LoanQueryVo {

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

    /**
     * 审核原因
     */
    private String reason;

    /**
     * 审核原因英文
     */
    private String reason_en;

    /**
     * 还款金额
     */
    private BigDecimal repayment_amount;
    /**
     * 剩余应还金额
     */
    private BigDecimal remaining_amount_due;

    /**
     * 创建时间
     */
    private LocalDateTime create_time_ms;

    private String image_1;

    private String image_2;

    private String image_3;

    private String image_4;

    public static LoanQueryVo convert(Loan loan) {
        if (ObjectUtil.isNull(loan)) {
            return null;
        }
        LoanQueryVo loanQueryVo = BeanUtil.copyProperties(loan, LoanQueryVo.class);
        BigDecimal amount = ObjectUtil.isNull(loan.getActual_amount()) ? loan.getExpect_amount() : loan.getActual_amount();
        loanQueryVo.setEstimate_interest(amount.multiply(loan.getRate()).multiply(Convert.toBigDecimal(loan.getRepayment_cycle())));
        if (ObjectUtil.isNotNull(loan.getActual_amount()) && loan.getActual_amount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal repayment_amount = ObjectUtil.isNull(loan.getRepayment_amount()) ? BigDecimal.ZERO : loan.getRepayment_amount();
            loanQueryVo.setRemaining_amount_due((loan.getActual_amount().add(loan.getInterest()).add(loan.getForfeit_penalty())).subtract(repayment_amount));
        }
        return loanQueryVo;
    }
}
