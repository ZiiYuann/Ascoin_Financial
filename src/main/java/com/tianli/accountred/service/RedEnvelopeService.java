package com.tianli.accountred.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.query.RedEnvelopeChainQuery;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.query.RedEnvelopeIoUQuery;
import com.tianli.accountred.vo.RedEnvelopeGetDetailsVO;
import com.tianli.accountred.vo.RedEnvelopeGetVO;
import com.tianli.accountred.vo.RedEnvelopeGiveRecordVO;
import com.tianli.common.PageQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public interface RedEnvelopeService extends IService<RedEnvelope> {

    /**
     * 发红包(对于上链红包来说还有二次调用)
     *
     * @param uid   用户id
     * @param query 发红包参数
     */
    Long give(Long uid, RedEnvelopeIoUQuery query);

    /**
     * 链上红包发放
     */
    Long give(Long uid, RedEnvelopeChainQuery query);

    /**
     * 抢红包
     */
    RedEnvelopeGetVO get(Long uid, RedEnvelopeGetQuery query);

    /**
     * 抢红包详情
     */
    RedEnvelopeGetDetailsVO getDetails(Long uid, Long rid);

    /**
     * 发红包记录
     */
    IPage<RedEnvelopeGiveRecordVO> giveRecord(Long uid, PageQuery<RedEnvelope> pageQuery);

}

