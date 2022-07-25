package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BorrowOrderConfigBO {
    private Long id;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 最小可借
     */
    private BigDecimal minimumBorrow;

    /**
     * 最大可借
     */
    private BigDecimal maximumBorrow;

    /**
     * 年利率
     */
    private BigDecimal annualInterestRate;
}
