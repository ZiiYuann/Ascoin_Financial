package com.tianli.accountred.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.query.RedEnvelopeGetQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public interface RedEnvelopeSpiltService extends IService<RedEnvelopeSpilt> {

    /**
     * 拆分红包,并且会把拆分id缓存到redis中
     *
     * @param redEnvelope 红包信息
     */
    void spiltRedEnvelope(RedEnvelope redEnvelope);

    /**
     * 领取拆分红包
     *
     * @param uid                 用户id
     * @param shortUid            用户id短码
     * @param uuid                拆分红包id
     * @param redEnvelopeGetQuery 领取红包参数
     * @return 领取信息
     */
    RedEnvelopeSpilt getRedEnvelopeSpilt(Long uid, Long shortUid, String uuid, RedEnvelopeGetQuery redEnvelopeGetQuery);
}
