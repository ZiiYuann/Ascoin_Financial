package com.tianli.accountred.vo;


import com.tianli.accountred.enums.RedEnvelopeType;
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

    private String coin;

    private BigDecimal amount;

    private Long uid;

    private Long shortUid;

    private Long redShortUid;

    private LocalDateTime receiveTime;

    private RedEnvelopeType type;

    private String remarks;

}
