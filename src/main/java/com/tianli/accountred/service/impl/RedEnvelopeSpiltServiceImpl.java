package com.tianli.accountred.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
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
import com.tianli.accountred.vo.RedEnvelopeExternGetDetailsVO;
import com.tianli.accountred.vo.RedEnvelopeExternGetRecordVO;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.ErrorCodeEnum;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private StringRedisTemplate stringRedisTemplate;
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
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private WebHookService webHookService;

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
            String keyOffSite = RedisConstants.SPILT_RED_ENVELOPE_CODE + redEnvelope.getId();
            Set<ZSetOperations.TypedTuple<String>> typedTuples = new HashSet<>(spiltRedEnvelopes.size());
            for (int i = 0; i < spiltRedEnvelopes.size(); i++) {
                RedEnvelopeSpilt redEnvelopeSpilt = spiltRedEnvelopes.get(i);
                typedTuples.add(new DefaultTypedTuple<>(JSONUtil.toJsonStr(redEnvelopeSpilt), (double) i));
            }

            stringRedisTemplate.opsForZSet().add(keyOffSite, typedTuples);
            redisTemplate.expire(keyOffSite, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);
        }
    }

    @Override
    @Transactional
    public RedEnvelopeSpilt getRedEnvelopeSpilt(Long uid, Long shortUid, String uuid, RedEnvelopeGetQuery redEnvelopeGetQuery) {
        LocalDateTime receiveTime = LocalDateTime.now();
        uuid = uuid.replace("\"", "");
        // 修改拆分（子）红包的状态
        int i = this.getBaseMapper().receive(redEnvelopeGetQuery.getRid(), uuid, receiveTime);
        if (i == 0) {
            webHookService.dingTalkSend("红包领取状态异常，请排查【2】");
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
    public RedEnvelopeExchangeCodeVO getExternOperationRedis(RedEnvelope redEnvelope) {
        var rid = redEnvelope.getId();
        UUID uuid = UUID.randomUUID();
        String keyOffSite = RedisConstants.SPILT_RED_ENVELOPE_CODE + rid;
        String mapKey = RedisConstants.SPILT_RED_ENVELOPE_CODE + uuid;
        long now = System.currentTimeMillis();
        // 取出小于当前时间的红包，并且设置一个新的过期时间（当前时间 + 2小时）
        String script = "local key = KEYS[1]\n" +
                "local key2 = KEYS[2]\n" +
                "local currentMs = tonumber(ARGV[1]) \n" +
                "local uuid = ARGV[2] \n" +
                "local termOfValidity =  2 * 60 * 60 \n" +
                "if  redis.call('EXISTS', key) == 0 then\n" +
                "    return 'NOT_EXIST'\n" +
                "end\n" +
                "local spiltReds = redis.call('ZRANGEBYSCORE',key,0,currentMs,'LIMIT',0,1)\n" +
                "if spiltReds[1] == nil then\n" +
                "    return 'FINISH'\n" +
                "end\n" +
                "local score = currentMs + termOfValidity * 1000 \n" +
                "redis.call('ZADD',key,score,spiltReds[1])\n" +
                "redis.call('SET',key2,spiltReds[1])\n" +
                "redis.call('EXPIRE',key2,termOfValidity)\n" +
                "return spiltReds[1]";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        String result = stringRedisTemplate.opsForValue().getOperations().execute(redisScript, List.of(keyOffSite, mapKey)
                , String.valueOf(now), uuid.toString());
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
        RedEnvelopeSpilt redEnvelopeSpilt = JSONUtil.toBean(result, RedEnvelopeSpilt.class);
        return RedEnvelopeExchangeCodeVO.builder()
                .receiveAmount(redEnvelopeSpilt.getAmount())
                .exchangeCode(uuid.toString())
                .coin(redEnvelope.getCoin())
                .usdtRate(currencyService.huobiUsdtRate(redEnvelope.getCoin()))
                .usdtCnyRate(BigDecimal.valueOf(digitalCurrencyExchange.usdtCnyPrice()))
                .totalAmount(redEnvelope.getTotalAmount())
                .build();


    }

    @Override
    @SuppressWarnings("unchecked")
    public RedEnvelopeExternGetDetailsVO getExternDetailsRedis(RedEnvelope redEnvelope) {
        String coin = redEnvelope.getCoin();
        CoinBase coinBase = coinBaseService.getByName(coin);

        long now = System.currentTimeMillis();
        String key = RedisConstants.SPILT_RED_ENVELOPE_CODE + redEnvelope.getId();

//        Set<ZSetOperations.TypedTuple<String>> redisRecords =
//                Optional.ofNullable(stringRedisTemplate.opsForZSet().rangeByScoreWithScores(key
//                                , 1670774400000f,now, 0, 50))
//                        .orElse(SetUtils.EMPTY_SORTED_SET);
//        redisRecords.stream().map(typedTuple -> {
//            RedEnvelopeSpilt redEnvelopeSpilt = JSONUtil.toBean(typedTuple.getValue(), RedEnvelopeSpilt.class);
//
//            Double score =
//                    Optional.ofNullable(typedTuple.getScore()).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
//            LocalDateTime receiveTime =
//                    LocalDateTime.ofEpochSecond(score.longValue() / 1000, 0, ZoneOffset.ofHours(8));
//
//            return RedEnvelopeExternGetRecordVO.builder()
//                    .receiveTime(receiveTime)
//                    .amount(redEnvelopeSpilt.getAmount())
//                    // TODO 设置昵称头像
//                    .headLogo("")
//                    .nickName("").build();
//        })


        Long noReceiveNum = MoreObjects.firstNonNull(
                stringRedisTemplate.opsForZSet().count(key, 1670774400000f, Double.MAX_VALUE),
                0L);

        RedEnvelopeExternGetDetailsVO.builder()
                .coin(coin)
                .coinUrl(coinBase.getLogo())
                .num(redEnvelope.getNum())
                .receiveNum(redEnvelope.getNum() - noReceiveNum.intValue())
                .shortUid(redEnvelope.getShortUid())
                .uid(redEnvelope.getUid())
                .totalAmount(redEnvelope.getTotalAmount())
                .remarks(redEnvelope.getRemarks())
                .status(redEnvelope.getStatus())
                .usdtRate(currencyService.getDollarRate(coin))
                .build();
        return null;
    }

}
