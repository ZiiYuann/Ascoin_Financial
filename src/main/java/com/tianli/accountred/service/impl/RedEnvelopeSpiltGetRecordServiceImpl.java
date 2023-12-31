package com.tianli.accountred.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.accountred.convert.RedEnvelopeConvert;
import com.tianli.accountred.dto.RedEnvelopeGetDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.mapper.RedEnvelopeSpiltGetRecordMapper;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.accountred.vo.RedEnvelopeSpiltGetRecordVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.sqs.context.RedEnvelopeContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedEnvelopeConvert redEnvelopeConvert;
    @Resource
    private RedEnvelopeService redEnvelopeService;

    @Override
    public RedEnvelopeSpiltGetRecord getRecord(Long rid, Long uid) {
        return this.getBaseMapper().selectOne(new LambdaQueryWrapper<RedEnvelopeSpiltGetRecord>()
                .eq(RedEnvelopeSpiltGetRecord::getRid, rid)
                .eq(RedEnvelopeSpiltGetRecord::getUid, uid));
    }

    @Override
    public List<RedEnvelopeSpiltGetRecord> getRecords(RedEnvelope redEnvelope) {
        PageQuery<RedEnvelopeSpiltGetRecord> pageQuery = new PageQuery<>(0, Integer.MAX_VALUE);
        return getRecords(redEnvelope, pageQuery).getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPage<RedEnvelopeSpiltGetRecord> getRecords(RedEnvelope redEnvelope, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {
        Page<RedEnvelopeSpiltGetRecord> page = pageQuery.page();
        if (redEnvelope.getReceiveNum() == 0) {
            page.setRecords((List<RedEnvelopeSpiltGetRecord>) CollectionUtils.EMPTY_COLLECTION);
            return page;
        }

        String getRecordsKey = RedisConstants.RED_ENVELOPE_RECORD + redEnvelope.getId();

        Boolean hasKey = Optional.ofNullable(stringRedisTemplate.hasKey(getRecordsKey)).orElse(Boolean.FALSE);

        if (Boolean.FALSE.equals(hasKey)) {
            List<RedEnvelopeSpiltGetRecord> records = this.list(new LambdaQueryWrapper<RedEnvelopeSpiltGetRecord>()
                    .eq(RedEnvelopeSpiltGetRecord::getRid, redEnvelope.getId()));
            var typedTuples = records.stream().map(index -> {
                long score = index.getReceiveTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                return ZSetOperations.TypedTuple.of(JSONUtil.toJsonStr(index), (double) score);
            }).collect(Collectors.toSet());
            stringRedisTemplate.opsForZSet().add(getRecordsKey, typedTuples);
            stringRedisTemplate.expire(getRecordsKey, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);
        }

        long now = System.currentTimeMillis();
        Set<String> recordsCache = stringRedisTemplate.opsForZSet().rangeByScore(getRecordsKey
                , 0, now
                , pageQuery.getPage(), pageQuery.getPageSize());

        Long count = stringRedisTemplate.opsForZSet().count(getRecordsKey, 0, now);

        List<RedEnvelopeSpiltGetRecord> records =
                new ArrayList<>(MoreObjects.firstNonNull(recordsCache, (Set<String>) SetUtils.EMPTY_SORTED_SET))
                        .stream()
                        .map(str -> JSONUtil.toBean(str, RedEnvelopeSpiltGetRecord.class))
                        .collect(Collectors.toList());
        page.setRecords(records);
        page.setTotal(MoreObjects.firstNonNull(count, 0L));
        return page;
    }

    @Override
    public List<RedEnvelopeSpiltGetRecord> getRecords(Long rid) {
        RedEnvelope redEnvelope = redEnvelopeService.getById(rid);
        return this.getRecords(redEnvelope);
    }

    @Override
    public List<RedEnvelopeSpiltGetRecordVO> getRecordVos(RedEnvelope redEnvelope) {
        List<RedEnvelopeSpiltGetRecord> index = this.getRecords(redEnvelope, new PageQuery<>(0, 1000000))
                .getRecords();
        return index.stream().map(redEnvelopeConvert::toRedEnvelopeSpiltGetRecordVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecordFlow(Long uid, Long shortUid, String uuid
            , RedEnvelopeGetDTO redEnvelopeGetDTO, RedEnvelopeSpilt redEnvelopeSpilt) {
        RedEnvelope redEnvelope = redEnvelopeGetDTO.getRedEnvelope();
        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord = RedEnvelopeSpiltGetRecord.builder()
                .amount(redEnvelopeSpilt.getAmount())
                .id(CommonFunction.generalId())
                .uid(uid)
                .shortUid(shortUid)
                .redShortUid(redEnvelope.getShortUid())
                .coin(redEnvelope.getCoin())
                .sRid(uuid)
                .rid(redEnvelope.getId())
                .type(redEnvelope.getType())
                .receiveTime(redEnvelopeSpilt.getReceiveTime())
                .deviceNumber(redEnvelopeGetDTO.getDeviceNumber())
                .exchangeCode(redEnvelopeGetDTO.getExchangeCode())
                .build();

        this.save(redEnvelopeSpiltGetRecord);
        long score = redEnvelopeSpiltGetRecord.getReceiveTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        String getRecordsKey = RedisConstants.RED_ENVELOPE_RECORD + redEnvelope.getId();
        stringRedisTemplate.opsForZSet().add(getRecordsKey, JSONUtil.toJsonStr(redEnvelopeSpiltGetRecord), score);
        stringRedisTemplate.expire(getRecordsKey, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);

        return redEnvelopeSpiltGetRecord;
    }

    @Override
    public IPage<RedEnvelopeSpiltGetRecordVO> getRecords(Long uid, RedEnvelopeChannel channel, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {
        LambdaQueryWrapper<RedEnvelopeSpiltGetRecord> queryWrapper = new LambdaQueryWrapper<RedEnvelopeSpiltGetRecord>()
                .eq(RedEnvelopeSpiltGetRecord::getUid, uid)
                .last(" order by receive_time desc");

        if (RedEnvelopeChannel.EXTERN.equals(channel)) {
            queryWrapper = queryWrapper.isNotNull(RedEnvelopeSpiltGetRecord::getExchangeCode);
        }
        if (RedEnvelopeChannel.CHAT.equals(channel)) {
            queryWrapper = queryWrapper.isNull(RedEnvelopeSpiltGetRecord::getExchangeCode);
        }

        return this.getBaseMapper().selectPage(pageQuery.page(), queryWrapper)
                .convert(index -> {
                    var vo = redEnvelopeConvert.toRedEnvelopeSpiltGetRecordVO(index);
                    RedEnvelope redEnvelope = redEnvelopeService.getById(index.getRid());
                    vo.setRemarks(redEnvelope.getRemarks());
                    return vo;
                });
    }

    @Override
    public BigDecimal receivedAmount(Long rid) {
        return MoreObjects.firstNonNull(baseMapper.receivedAmount(rid), BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void compensate(Long id) {
        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord = this.getById(id);
        redEnvelopeService.asynGet(RedEnvelopeContext.builder()
                .rid(redEnvelopeSpiltGetRecord.getRid())
                .uuid(redEnvelopeSpiltGetRecord.getSRid())
                .uid(redEnvelopeSpiltGetRecord.getUid())
                .shortUid(redEnvelopeSpiltGetRecord.getShortUid())
                .exchangeCode(redEnvelopeSpiltGetRecord.getExchangeCode())
                .deviceNumber(redEnvelopeSpiltGetRecord.getDeviceNumber())
                .build()
        );
    }

}
