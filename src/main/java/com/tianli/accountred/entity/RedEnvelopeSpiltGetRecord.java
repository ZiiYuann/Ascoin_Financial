package com.tianli.accountred.entity;

import com.tianli.accountred.enums.RedEnvelopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领取明细
 *
 * @author chenb
 * @apiNote
 * @since 2022-10-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeSpiltGetRecord {

    @Id
    private Long id;

    private Long rid;

    private Long uid;

    private Long shortUid;

    private Long redShortUid;

    private String sRid;

    private String coin;

    private BigDecimal amount;

    private LocalDateTime receiveTime;

    private RedEnvelopeType type;

    private String deviceNumber;

}
