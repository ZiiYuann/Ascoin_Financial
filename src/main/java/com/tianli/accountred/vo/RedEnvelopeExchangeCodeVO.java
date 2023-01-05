package com.tianli.accountred.vo;

import com.tianli.accountred.enums.RedEnvelopeStatus;
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
public class RedEnvelopeExchangeCodeVO {

    private RedEnvelopeStatus status;

    private String exchangeCode;

    /**
     * 红包的总金额
     */
    private BigDecimal totalAmount;

    /**
     * 领取到到金额
     */
    private BigDecimal receiveAmount;

    private String coin;

    /**
     * 折合u汇率
     */
    private BigDecimal usdtRate;

    /**
     * usdt转cny汇率
     */
    private BigDecimal usdtCnyRate;

    public RedEnvelopeExchangeCodeVO(RedEnvelopeStatus status) {
        this.status = status;
    }
}
