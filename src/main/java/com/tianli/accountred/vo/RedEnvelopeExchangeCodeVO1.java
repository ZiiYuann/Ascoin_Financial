package com.tianli.accountred.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-29
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeExchangeCodeVO1 {

    private String exchangeCode;

    private BigDecimal receiveAmount;

    private String coin;

    private String flag;

    private String coinUrl;

    private BigDecimal usdtRate;
}
