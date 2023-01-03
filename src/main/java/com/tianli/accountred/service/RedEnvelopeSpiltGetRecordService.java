package com.tianli.accountred.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.vo.RedEnvelopeSpiltGetRecordVO;
import com.tianli.common.PageQuery;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-20
 **/
public interface RedEnvelopeSpiltGetRecordService {

    /**
     * 获取红包领取记录
     *
     * @param rid 红包id
     * @param uid 领取人id
     * @return 红包领取记录
     */
    RedEnvelopeSpiltGetRecord getRecord(Long rid, Long uid);

    /**
     * 获取红包领取记录
     *
     * @param rid 红包id
     * @return 红包领取记录
     */
    List<RedEnvelopeSpiltGetRecord> getRecords(Long rid);

    /**
     * 获取红包领取记录VO
     *
     * @param rid 红包id
     * @return 红包领取记录
     */
    List<RedEnvelopeSpiltGetRecordVO> getRecordVos(Long rid);

    /**
     * 生成子红包领取流水
     *
     * @param uid                 用户id
     * @param shortUid            用户id短码
     * @param uuid                拆分红包id
     * @param redEnvelopeGetQuery 请求参数
     * @param redEnvelopeSpilt    子红包
     * @return 拆分红包领取记录（流水）
     */
    RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecordFlow(Long uid, Long shortUid, String uuid
            , RedEnvelopeGetQuery redEnvelopeGetQuery, RedEnvelopeSpilt redEnvelopeSpilt);

    /**
     * 领取红包记录
     */
    IPage<RedEnvelopeSpiltGetRecordVO> getRecord(Long uid, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery);

}
