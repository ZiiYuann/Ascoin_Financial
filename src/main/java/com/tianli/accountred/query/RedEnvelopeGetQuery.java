package com.tianli.accountred.query;

import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抢红包query
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGetQuery {

    /**
     * 红包id
     */
    private Long rid;

    /**
     * 红包唯一标示符
     */
    private String flag;

    private RedEnvelope redEnvelope;

}
