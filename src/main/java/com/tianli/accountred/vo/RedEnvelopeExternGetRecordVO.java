package com.tianli.accountred.vo;


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
public class RedEnvelopeExternGetRecordVO {

    private BigDecimal amount;

    private LocalDateTime receiveTime;

    private String headLogo;

    private String nickName;

    private boolean receive;

}
