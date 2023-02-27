package com.tianli.accountred.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.query.RedEnvelopeChainQuery;
import com.tianli.accountred.query.RedEnvelopeExchangeCodeQuery;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.query.RedEnvelopeGiveRecordQuery;
import com.tianli.accountred.query.RedEnvelopeIoUQuery;
import com.tianli.accountred.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.sqs.context.RedEnvelopeContext;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public interface RedEnvelopeService extends IService<RedEnvelope> {

    /**
     * 发红包(对于上链红包来说还有二次调用)
     *
     * @param uid      用户id
     * @param shortUid 用户id
     * @param query    发红包参数
     */
    Result<RedEnvelopeGiveVO> give(Long uid, Long shortUid, RedEnvelopeIoUQuery query);

    /**
     * 链上红包发放
     */
    Result<RedEnvelopeGiveVO> give(Long uid, Long shortUid, RedEnvelopeChainQuery query);

    /**
     * 抢红包（聊天）
     */
    RedEnvelopeGetVO get(Long uid, Long shortUid, RedEnvelopeGetQuery query);

    /**
     * 兑换红包
     */
    RedEnvelopeGetVO get(Long uid, Long shortUid, RedEnvelopeExchangeCodeQuery query);

    /**
     * 抢红包详情
     */
    RedEnvelopeGetDetailsVO getDetails(Long uid, Long rid);

    /**
     * 发红包记录
     */
    IPage<RedEnvelopeGiveRecordVO> giveRecord(RedEnvelopeGiveRecordQuery query, PageQuery<RedEnvelope> pageQuery);

    /**
     * 异步领取红包
     *
     * @param sqsContext sqs信息
     */
    void asynGet(RedEnvelopeContext sqsContext);

    /**
     * 红包到期
     */
    void expireRed(LocalDateTime now);

    /**
     * 站外红包手动回退
     */
    void backRed(Long uid, Long rid);

    /**
     * 获取红包信息
     */
    RedEnvelopeGetVO getInfoById(Long id);

    RedEnvelope getWithCache(Long id);

    RedEnvelope getWithCache(Long uid, Long id);

    void deleteRedisCache(Long id);


}


