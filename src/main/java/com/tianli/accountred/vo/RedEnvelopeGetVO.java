package com.tianli.accountred.vo;

import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    private BigDecimal uReceiveAmount;

    private CurrencyCoin coin;

    private Long uid;

    private Long shortUid;

    private String coinUrl;

    public RedEnvelopeGetVO(RedEnvelopeStatus status,CurrencyCoin coin) {
        this.status = status;
        this.coin = coin;
    }

    public String getCoinUrl() {
        return coin.getLogoPath();
    }
}
