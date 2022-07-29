package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

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


    public void convertToRate(){
        if(Objects.nonNull(annualInterestRate)) this.setAnnualInterestRate(annualInterestRate.divide(BigDecimal.valueOf(100) ,8, RoundingMode.UP));
    }
}
