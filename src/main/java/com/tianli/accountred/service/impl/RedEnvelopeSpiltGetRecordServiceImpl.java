package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.accountred.convert.RedEnvelopeConvert;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.mapper.RedEnvelopeSpiltGetRecordMapper;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.accountred.vo.RedEnvelopeSpiltGetRecordVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-20
 **/
@Service
public class RedEnvelopeSpiltGetRecordServiceImpl extends ServiceImpl<RedEnvelopeSpiltGetRecordMapper, RedEnvelopeSpiltGetRecord>
        implements RedEnvelopeSpiltGetRecordService {

    @Resource
    private RedisService redisService;
    @Resource
    private RedEnvelopeConvert redEnvelopeConvert;

    @Override
    public RedEnvelopeSpiltGetRecord getRecord(Long rid, Long uid) {
        return this.getBaseMapper().selectOne(new LambdaQueryWrapper<RedEnvelopeSpiltGetRecord>()
                .eq(RedEnvelopeSpiltGetRecord::getRid, rid)
                .eq(RedEnvelopeSpiltGetRecord::getUid, uid));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RedEnvelopeSpiltGetRecord> getRecords(Long rid) {

        String getRecordsKey = RedisConstants.RED_ENVELOPE_GET_RECORD + rid;

        Object cache = redisService.get(getRecordsKey);
        if (Objects.isNull(cache)) {
            List<RedEnvelopeSpiltGetRecord> getRecords = this.list(new LambdaQueryWrapper<RedEnvelopeSpiltGetRecord>()
                    .eq(RedEnvelopeSpiltGetRecord::getRid, rid));

            redisService.set(RedisConstants.RED_ENVELOPE_GET_RECORD + rid, getRecords, 3L, TimeUnit.DAYS);
            return getRecords;
        }

        return (List<RedEnvelopeSpiltGetRecord>) cache;
    }

    @Override
    public List<RedEnvelopeSpiltGetRecordVO> getRecordVos(Long rid) {
        List<RedEnvelopeSpiltGetRecord> record = this.getRecords(rid);
        return record.stream().map(redEnvelopeConvert::toRedEnvelopeSpiltGetRecordVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecordFlow(Long uid, Long shortUid, String uuid
            , RedEnvelopeGetQuery redEnvelopeGetQuery, RedEnvelopeSpilt redEnvelopeSpilt) {

        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord = RedEnvelopeSpiltGetRecord.builder()
                .amount(redEnvelopeSpilt.getAmount())
                .id(CommonFunction.generalId())
                .uid(uid)
                .shortUid(shortUid)
                .coin(redEnvelopeGetQuery.getRedEnvelope().getCoin())
                .sRid(uuid)
                .rid(redEnvelopeGetQuery.getRid())
                .type(redEnvelopeGetQuery.getRedEnvelope().getType())
                .receiveTime(redEnvelopeSpilt.getReceiveTime())
                .build();

        this.save(redEnvelopeSpiltGetRecord);

        return redEnvelopeSpiltGetRecord;
    }

    @Override
    public IPage<RedEnvelopeSpiltGetRecordVO> getRecord(Long uid, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {
        LambdaQueryWrapper<RedEnvelopeSpiltGetRecord> queryWrapper = new LambdaQueryWrapper<RedEnvelopeSpiltGetRecord>()
                .eq(RedEnvelopeSpiltGetRecord::getUid, uid);

        return this.getBaseMapper().selectPage(pageQuery.page(), queryWrapper)
                .convert(redEnvelopeConvert::toRedEnvelopeSpiltGetRecordVO);
    }
}
