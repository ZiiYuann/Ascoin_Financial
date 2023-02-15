package com.tianli.accountred.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.accountred.dto.RedEnvelopStatusDTO;
import com.tianli.accountred.dto.RedEnvelopeGetDTO;
import com.tianli.accountred.dto.RedEnvelopeSpiltDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import com.tianli.accountred.vo.RedEnvelopeExternGetDetailsVO;
import com.tianli.common.PageQuery;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public interface RedEnvelopeSpiltService extends IService<RedEnvelopeSpilt> {

    /**
     * 领取拆分红包
     *
     * @param uid               用户id
     * @param shortUid          用户id短码
     * @param uuid              拆分红包id
     * @param redEnvelopeGetDTO 领取红包参数
     */
    RedEnvelopeSpiltGetRecord getRedEnvelopeSpilt(Long uid, Long shortUid, String uuid, RedEnvelopeGetDTO redEnvelopeGetDTO);

    /**
     * 拆分红包,并且会把拆分id缓存到redis中
     *
     * @param redEnvelope 红包信息
     */
    void spiltRedEnvelope(RedEnvelope redEnvelope);

    /**
     * 获取子红包
     *
     * @param rid     红包id
     * @param receive 领取状态
     * @return 红包列表
     */
    List<RedEnvelopeSpilt> getRedEnvelopeSpilt(Long rid, boolean receive);

    /**
     * 获取兑换码
     *
     */
    RedEnvelopeExchangeCodeVO getExchangeCode(Long rid, String ipKey, String fingerprintKey);

    /**
     * 获取临近当前时间最近的过期 时间信息
     */
    RedEnvelopStatusDTO getLatestExpireDTO(String externKey, long now);

    /**
     * 获取小于当前时间红包状态信息
     */
    RedEnvelopStatusDTO getNotExpireDTO(String externKey, long now);

    RedEnvelopStatusDTO getIpOrFingerDTO(String ip, String fingerprint);

    /**
     * 获取红包领取记录VO（站外实未领取，分页50条一页）
     *
     * @param redEnvelope 红包参数
     * @param pageQuery   分页参数
     * @return 红包领取记录
     */
    RedEnvelopeExternGetDetailsVO getExternDetailsRedis(RedEnvelope redEnvelope, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery);

    /**
     * 根据兑换码获取拆分红包信息
     *
     * @param exchangeCode 兑换码
     * @return 拆分红包信息
     */
    RedEnvelopeSpiltDTO getRedEnvelopeSpiltDTOCache(String exchangeCode);

}
