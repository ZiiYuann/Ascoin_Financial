package com.tianli.accountred.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.chain.entity.CoinBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGetVO {

    /**
     * 钱包状态
     */
    private RedEnvelopeStatus status;

    /**
     * 领取到到金额
     */
    private BigDecimal receiveAmount;

    /**
     * 领取到到金额 折合u
     */
    @JsonProperty
    private BigDecimal uReceiveAmount;

    private String coin;

    private Long uid;

    private Long shortUid;

    private String coinUrl;

    public RedEnvelopeGetVO(RedEnvelopeStatus status, CoinBase coin) {
        this.status = status;
        this.coin = coin.getName();
        this.coinUrl = coin.getLogo();
    }

    @JsonIgnore
    public BigDecimal getureceiveAmount() {
        return uReceiveAmount;
    }

}
