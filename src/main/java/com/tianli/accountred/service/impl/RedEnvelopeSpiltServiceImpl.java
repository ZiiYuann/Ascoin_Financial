package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.mapper.RedEnvelopeSpiltMapper;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisConstants;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Service
public class RedEnvelopeSpiltServiceImpl extends ServiceImpl<RedEnvelopeSpiltMapper, RedEnvelopeSpilt> implements RedEnvelopeSpiltService {

    // 1670774400000

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedEnvelopeSpiltGetRecordService redEnvelopeSpiltGetRecordService;
    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;

    @Override
    @Transactional
    public void spiltRedEnvelope(RedEnvelope redEnvelope) {
        List<RedEnvelopeSpilt> spiltRedEnvelopes = GiveStrategyAdapter.split(redEnvelope);

        String key = RedisConstants.SPILT_RED_ENVELOPE + redEnvelope.getId();
        this.saveBatch(spiltRedEnvelopes);
        redisTemplate.opsForSet().add(key, spiltRedEnvelopes.stream().map(RedEnvelopeSpilt::getId).toArray());
        redisTemplate.expire(key, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);

        // 如果是站外红包，额外设置 zset 缓存 （score 从0 开始）
        if (RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
            String keyOffSite = RedisConstants.SPILT_RED_ENVELOPE_OFF_SITE + redEnvelope.getId();
            Set<ZSetOperations.TypedTuple<Object>> typedTuples = new HashSet<>(spiltRedEnvelopes.size());
            for (int i = 0; i < spiltRedEnvelopes.size(); i++) {
                typedTuples.add(new DefaultTypedTuple<>(spiltRedEnvelopes.get(i).getId(), (double) i));
            }
            redisTemplate.opsForZSet().add(keyOffSite, typedTuples);
            redisTemplate.expire(keyOffSite, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);
        }
    }

    @Override
    @Transactional
    public RedEnvelopeSpilt getRedEnvelopeSpilt(Long uid, Long shortUid, String uuid, RedEnvelopeGetQuery redEnvelopeGetQuery) {
        // 如果qps高可以异步转账
        LocalDateTime receiveTime = LocalDateTime.now();
        uuid = uuid.replace("\"", "");
        // 修改拆分（子）红包的状态
        int i = this.getBaseMapper().receive(redEnvelopeGetQuery.getRid(), uuid, receiveTime);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }

        RedEnvelopeSpilt redEnvelopeSpilt = this.getById(uuid);


        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord =
                redEnvelopeSpiltGetRecordService.redEnvelopeSpiltGetRecordFlow(uid, shortUid, uuid, redEnvelopeGetQuery, redEnvelopeSpilt);

        String coin = redEnvelopeGetQuery.getRedEnvelope().getCoin();
        // 红包订单
        Order order = Order.builder()
                .uid(uid)
                .coin(coin)
                .orderNo(AccountChangeType.red_get.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(redEnvelopeSpilt.getAmount())
                .type(ChargeType.red_get)
                .completeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .status(ChargeStatus.chain_success)
                // 关联领取订单详情
                .relatedId(redEnvelopeSpiltGetRecord.getId())
                .build();
        orderService.save(order);
        // 操作账户
        accountBalanceServiceImpl.increase(uid, ChargeType.red_get, coin, redEnvelopeSpilt.getAmount(), order.getOrderNo(), "抢红包获取");

        return redEnvelopeSpilt;
    }

    @Override
    public List<RedEnvelopeSpilt> getRedEnvelopeSpilt(Long rid, boolean receive) {
        return this.list(new LambdaQueryWrapper<RedEnvelopeSpilt>()
                .eq(RedEnvelopeSpilt::getRid, rid)
                .eq(RedEnvelopeSpilt::isReceive, receive));
    }
}
