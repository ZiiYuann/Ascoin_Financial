package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BorrowOrderRepayBO {

    @NotNull
    private Long orderId;

    @DecimalMin(value = "0.00",message = "还款金额小于0")
    private BigDecimal repayAmount;

    /**
     * 币别
     */
    @NotNull(message = "币别不能为空")
    private CurrencyCoin currencyCoin;
}
