package com.tianli.management.vo;

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

    private String coin;

    private BigDecimal amount;

}
