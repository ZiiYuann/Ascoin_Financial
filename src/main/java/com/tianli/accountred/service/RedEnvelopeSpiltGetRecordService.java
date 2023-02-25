package com.tianli.accountred.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.accountred.dto.RedEnvelopeGetDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.vo.RedEnvelopeSpiltGetRecordVO;
import com.tianli.common.PageQuery;

import java.math.BigDecimal;
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
     * @param redEnvelope 红包信息
     * @return 红包领取记录
     */
    List<RedEnvelopeSpiltGetRecord> getRecords(RedEnvelope redEnvelope);

    /**
     * 获取红包领取记录(分页)
     *
     * @param redEnvelope 红包信息
     * @param pageQuery   分页信息
     * @return 红包领取记录
     */
    IPage<RedEnvelopeSpiltGetRecord> getRecords(RedEnvelope redEnvelope, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery);

    /**
     * 获取红包领取记录
     *
     * @param rid 红包信息
     * @return 红包领取记录
     */
    List<RedEnvelopeSpiltGetRecord> getRecords(Long rid);

    /**
     * 获取红包领取记录VO
     *
     * @param redEnvelope 红包信息
     * @return 红包领取记录VO
     */
    List<RedEnvelopeSpiltGetRecordVO> getRecordVos(RedEnvelope redEnvelope);

    /**
     * 生成子红包领取流水
     *
     * @param uid               用户id
     * @param shortUid          用户id短码
     * @param uuid              拆分红包id
     * @param redEnvelopeGetDTO 请求参数
     * @param redEnvelopeSpilt  子红包
     * @return 拆分红包领取记录（流水）
     */
    RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecordFlow(Long uid, Long shortUid, String uuid
            , RedEnvelopeGetDTO redEnvelopeGetDTO, RedEnvelopeSpilt redEnvelopeSpilt);

    /**
     * 领取红包记录
     */
    IPage<RedEnvelopeSpiltGetRecordVO> getRecords(Long uid, RedEnvelopeChannel channel, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery);

    /**
     * 已经领取金额
     */
    BigDecimal receivedAmount(Long rid);

}
