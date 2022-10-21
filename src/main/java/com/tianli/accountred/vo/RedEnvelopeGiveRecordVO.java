package com.tianli.accountred.vo;

import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGiveRecordVO {

    private CurrencyCoin coin;

    private RedEnvelopeStatus status;

    private String remarks;

    private int num;

    private int receiveNum;

    private LocalDateTime createTime;

    private BigDecimal totalAmount;

    private RedEnvelopeType redEnvelopeType;

}
