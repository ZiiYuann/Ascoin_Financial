package com.tianli.account.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
public class AccountBalanceSimpleVO {

    private CurrencyCoin coin;

    private BigDecimal dollarRate;

    private BigDecimal balanceAmount;

    private BigDecimal balanceDollarAmount;
}
