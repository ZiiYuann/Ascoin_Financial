package com.tianli.financial.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.PurchaseTerm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeByRecordIdVO {

    private String productName;

    private String productNameEn;

    private ProductType productType;

    private PurchaseTerm productTerm;

    private BigDecimal rate;

    private CurrencyCoin coin;

    /**
     * 持有币
     */
    private BigDecimal holdAmount;

    /**
     * 累计收益
     */
    private BigDecimal accrueIncomeFee;

    /**
     * 昨日收益
     */
    private BigDecimal yesterdayIncomeFee;


}
