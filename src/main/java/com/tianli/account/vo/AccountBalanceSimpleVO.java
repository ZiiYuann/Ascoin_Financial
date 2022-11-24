package com.tianli.account.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
public class AccountBalanceSimpleVO {

    private String coin;

    private BigDecimal dollarRate;

    private BigDecimal balanceAmount;

    @BigDecimalFormat("0.00")
    private BigDecimal balanceDollarAmount;
}
