package com.tianli.accountred.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @since 2022-10-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGetDetailsVO {

    /**
     * 红包持有者名称
     */
    private Long uid;

    private Long shortUid;

    /**
     * 币别
     */
    private CurrencyCoin coin;

    private String coinUrl;

    /**
     * 红包的总金额
     */
    private BigDecimal totalAmount;

    /**
     * 红包数量
     */
    private int num;

    private int receiveNum;

    /**
     * 红包文案
     */
    private String remarks;

    /**
     * WAIT:等待发送 PROCESS:发送中 fAIL:发送失败(上链失败) FINISH:已经完成 OVERDUE:过期
     */
    private RedEnvelopeStatus status;

    /**
     * 领取到金额
     */
    private BigDecimal receiveAmount;

    @JsonProperty
    private BigDecimal uReceiveAmount;

    @JsonIgnore
    public BigDecimal getureceiveAmount() {
        return uReceiveAmount;
    }

    private List<RedEnvelopeSpiltGetRecordVO> records;

    public String getCoinUrl() {
        return coin.getLogoPath();
    }

}
