package com.tianli.accountred.dto;

import com.tianli.accountred.entity.RedEnvelope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGetDTO {

    private Long rid;

    private String deviceNumber;

    private String exchangeCode;

    private RedEnvelope redEnvelope;
}
