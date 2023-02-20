package com.tianli.accountred.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeConfigVO {

    private String coin;

    private int scale;

    private BigDecimal limitAmount;

    private BigDecimal minAmount;

    private int num;
}
