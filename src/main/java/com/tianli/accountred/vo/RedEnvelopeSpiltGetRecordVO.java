package com.tianli.accountred.vo;


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
public class RedEnvelopeSpiltGetRecordVO {

    private CurrencyCoin coin;

    private BigDecimal amount;

    private Long uid;

    private Long shortUid;

    private LocalDateTime receiveTime;

    private RedEnvelopeType type;

}
