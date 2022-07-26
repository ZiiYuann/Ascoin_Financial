package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BorrowOrderBO {
    /**
     * 借币数量
     */
    private BigDecimal borrowAmount;

    /**
     * 币别
     */
    @NotNull(message = "币别不能为空")
    private CurrencyCoin coin;

}
