package com.tianli.accountred.vo;

import com.tianli.accountred.enums.RedEnvelopeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public RedEnvelopeExchangeCodeVO(RedEnvelopeStatus status) {
        this.status = status;
    }
}
