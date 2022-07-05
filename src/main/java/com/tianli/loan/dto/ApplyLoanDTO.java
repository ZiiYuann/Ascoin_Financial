package com.tianli.loan.dto;

import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.entity.LoanCycle;
import com.tianli.loan.enums.LoanStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/5/26 11:12
 */
@Data
public class ApplyLoanDTO {

    /**
     * 期望借款金额
     */
    @NotNull(message = "参数错误")
    private BigDecimal expect_amount;

    /**
     * 还款周期id
     */
    @NotNull(message = "参数错误")
    private Long loan_cycle_id;

    @NotBlank(message = "参数错误")
    private String image_1;

    @NotBlank(message = "参数错误")
    private String image_2;

    @NotBlank(message = "参数错误")
    private String image_3;

    @NotBlank(message = "参数错误")
    private String image_4;

    public Loan getLoan(RequestInitService requestInitService, LoanCycle loanCycle) {
        return Loan.builder()
                .id(CommonFunction.generalId())
                .uid(requestInitService.uid())
                .expect_amount(expect_amount)
                .token(CurrencyCoinEnum.usdt.getName())
                .rate(loanCycle.getDay_rate())
                .late_fee_rate(loanCycle.getLate_fee_rate())
                .repayment_cycle(loanCycle.getRepayment_cycle())
                .repayment("到期一次还本息")
                .status(LoanStatusEnum.PENDING_REVIEW)
                .interest(BigDecimal.ZERO)
                .forfeit_penalty(BigDecimal.ZERO)
                .repayment_amount(BigDecimal.ZERO)
                .repayment_principal(BigDecimal.ZERO)
                .repayment_interest(BigDecimal.ZERO)
                .repayment_forfeit_penalty(BigDecimal.ZERO)
                .current_day(0)
                .create_time(requestInitService.now())
                .create_time_ms(requestInitService.now_ms())
                .image_1(image_1)
                .image_2(image_2)
                .image_3(image_3)
                .image_4(image_4)
                .build();
    }

}
