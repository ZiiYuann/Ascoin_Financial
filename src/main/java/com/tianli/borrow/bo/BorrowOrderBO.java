package com.tianli.borrow.bo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BorrowOrderBO {
    /**
     * 借币数量
     */
    @NotNull
    @DecimalMin(value = "0.00", message = "借币数量小于0")
    private BigDecimal borrowAmount;

    /**
     * 币别
     */
    @NotNull(message = "币别不能为空")
    private String coin;

}
