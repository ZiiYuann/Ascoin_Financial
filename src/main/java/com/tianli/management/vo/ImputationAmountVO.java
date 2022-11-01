package com.tianli.management.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImputationAmountVO {

    private BigDecimal totalAmount;

    private CurrencyCoin coin;

    private BigDecimal amount;

}
