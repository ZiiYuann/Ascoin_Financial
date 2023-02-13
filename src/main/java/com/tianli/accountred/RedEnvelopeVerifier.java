package com.tianli.accountred;

import com.tianli.accountred.entity.RedEnvelope;

/**
 * 红包校验器
 */
public interface RedEnvelopeVerifier {


    /**
     * 红包信息校验
     *
     * @param uid         用户id
     * @param redEnvelope 红包信息
     */
    void verifier(Long uid, RedEnvelope redEnvelope);

}
