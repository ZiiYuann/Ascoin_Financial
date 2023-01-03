package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.mapper.RedEnvelopeSpiltMapper;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisConstants;
import com.tianli.currency.service.CurrencyService;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.ErrorCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
    @Resource
    private CurrencyService currencyService;
    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchange;

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
                RedEnvelopeSpilt redEnvelopeSpilt = spiltRedEnvelopes.get(i);
                // 这里参数的顺序会影响 getByExternOperation 方法中脚本
                List<String> params = new ArrayList<>();
                params.add(redEnvelopeSpilt.getId());
                params.add(redEnvelopeSpilt.getAmount().toPlainString());
                typedTuples.add(new DefaultTypedTuple<>(StringUtils.join(params, ","), (double) i));
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

    @Override
    public RedEnvelopeExchangeCodeVO getByExternOperation(RedEnvelope redEnvelope) {
        var rid = redEnvelope.getId();
        UUID uuid = UUID.randomUUID();
        String keyOffSite = RedisConstants.SPILT_RED_ENVELOPE_OFF_SITE + rid;
        String mapKey = RedisConstants.SPILT_RED_ENVELOPE_OFF_SITE + uuid;
        long now = System.currentTimeMillis();
        // 取出小于当前时间的红包，并且设置一个新的过期时间（当前时间 + 2小时）
        String script = "" +
                "local key = KEYS[1]\n" +
                "local key2 = KEYS[2]\n" +
                "local currentMs = tonumber(ARGV[1]) \n" +
                "local uuid = ARGV[2] \n" +
                "local termOfValidity =  2 * 60 * 60 \n" +
                "\n" +
                "if  redis.call('EXISTS', key) == 0 then\n" +
                "    return 'NOT_EXIST'\n" +
                "end\n" +
                "local ids = redis.call('ZRANGEBYSCORE',key,0,currentMs,'LIMIT',0,1)\n" +
                "if ids[1] == nil then\n" +
                "    return 'FINISH'\n" +
                "end\n" +
                "local score = currentMs + termOfValidity * 1000 \n" +
                "redis.call('ZADD',key,score,ids[1])\n" +
                "redis.call('SET',key2,ids[1])\n" +
                "redis.call('EXPIRE',key2,termOfValidity)\n" +
                "return ids[1]";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        Object[] objects = new Object[]{String.valueOf(now), uuid.toString()};
        String result = redisTemplate.opsForValue().getOperations().execute(redisScript, List.of(keyOffSite, mapKey), objects);
        if (StringUtils.isBlank(result)) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        if ("NOT_EXIST".equals(result)) {
            log.error("站外红包ZSET不存在:" + rid);
            ErrorCodeEnum.throwException("站外红包ZSET不存在");
        }
        if ("FINISH".equals(result)) {
            return new RedEnvelopeExchangeCodeVO(RedEnvelopeStatus.FINISH);
        }
        result = result.replace("\"", "");

        String[] params = result.split(",");

        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeByScoreWithScores(keyOffSite
                , 1670774400000f, Double.MAX_VALUE, 0, 20);

        RedEnvelopeSpilt redEnvelopeSpilt = this.getById(params[1]);
        return RedEnvelopeExchangeCodeVO.builder()
                .receiveAmount(new BigDecimal(params[2]))
                .exchangeCode(uuid.toString())
                .coin(redEnvelope.getCoin())
                .usdtRate(currencyService.huobiUsdtRate(redEnvelope.getCoin()))
                .usdtCnyRate(BigDecimal.valueOf(digitalCurrencyExchange.usdtCnyPrice()))
                .totalAmount(redEnvelope.getTotalAmount())
                .build();
    }
}
