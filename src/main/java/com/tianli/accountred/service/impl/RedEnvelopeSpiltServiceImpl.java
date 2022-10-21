package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeType;
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
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Service
public class RedEnvelopeSpiltServiceImpl extends ServiceImpl<RedEnvelopeSpiltMapper, RedEnvelopeSpilt> implements RedEnvelopeSpiltService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedEnvelopeSpiltGetRecordService redEnvelopeSpiltGetRecordService;
    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceService accountBalanceService;

    private static final HashMap<RedEnvelopeType, RedEnvelopeGiveStrategy> GIVE_STRATEGY = new HashMap<>(4);

    static {
        NormalGiveStrategy normalGiveStrategy = new NormalGiveStrategy();
        RandomGiveStrategy randomGiveStrategy = new RandomGiveStrategy();
        GIVE_STRATEGY.put(RedEnvelopeType.NORMAL, normalGiveStrategy);
        GIVE_STRATEGY.put(RedEnvelopeType.PRIVATE, normalGiveStrategy);
        GIVE_STRATEGY.put(RedEnvelopeType.RANDOM, randomGiveStrategy);
    }

    @Override
    @Transactional
    public void spiltRedEnvelope(RedEnvelope redEnvelope) {
        List<RedEnvelopeSpilt> spiltRedEnvelopes = GIVE_STRATEGY.get(redEnvelope.getType())
                .spiltRedEnvelope(redEnvelope.getId(), redEnvelope.getNum(), redEnvelope.getAmount(), redEnvelope.getTotalAmount());

        String key = RedisConstants.SPILT_RED_ENVELOPE + redEnvelope.getId();
        this.saveBatch(spiltRedEnvelopes);
        redisTemplate.opsForSet().add(key, spiltRedEnvelopes.stream().map(RedEnvelopeSpilt::getId).toArray());
        redisTemplate.expire(key, 24, TimeUnit.HOURS);

    }

    @Override
    public RedEnvelopeSpilt getRedEnvelopeSpilt(Long uid, String uuid, RedEnvelopeGetQuery redEnvelopeGetQuery) {
        // 如果qps高可以异步转账
        LocalDateTime receiveTime = LocalDateTime.now();

        // 修改拆分（子）红包的状态
        int i = this.getBaseMapper().receive(redEnvelopeGetQuery.getRid(), uuid, receiveTime);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }

        RedEnvelopeSpilt redEnvelopeSpilt = this.getById(uuid);


        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord =
                redEnvelopeSpiltGetRecordService.redEnvelopeSpiltGetRecordFlow(uid, uuid, redEnvelopeGetQuery, redEnvelopeSpilt);

        CurrencyCoin coin = redEnvelopeGetQuery.getRedEnvelope().getCoin();
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
        accountBalanceService.increase(uid,ChargeType.red_get,coin,redEnvelopeSpilt.getAmount(),order.getOrderNo(),"抢红包获取");

        return redEnvelopeSpilt;
    }
}
