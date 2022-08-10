package com.tianli.management.dto;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-10
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmountDto {

    private BigDecimal amount;

    private CurrencyCoin coin;
}
