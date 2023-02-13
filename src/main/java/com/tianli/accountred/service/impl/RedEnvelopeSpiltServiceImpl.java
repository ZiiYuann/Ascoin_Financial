package com.tianli.accountred.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.accountred.convert.RedEnvelopeConvert;
import com.tianli.accountred.dto.RedEnvelopeGetDTO;
import com.tianli.accountred.dto.RedEnvelopeSpiltDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.mapper.RedEnvelopeSpiltMapper;
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
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.currency.service.CurrencyService;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.ErrorCodeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Service
public class RedEnvelopeSpiltServiceImpl extends ServiceImpl<RedEnvelopeSpiltMapper, RedEnvelopeSpilt> implements RedEnvelopeSpiltService {

    private static final long TIME_BEGIN = 1670774400000L;

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
    private RedEnvelopeConvert redEnvelopeConvert;


    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public RedEnvelopeSpiltGetRecord getRedEnvelopeSpilt(Long uid, Long shortUid, String uuid, RedEnvelopeGetDTO redEnvelopeGetDTO) {
        RedEnvelope redEnvelope = redEnvelopeGetDTO.getRedEnvelope();
        LocalDateTime receiveTime = LocalDateTime.now();
        uuid = uuid.replace("\"", "");

        // 修改拆分（子）红包的状态
        int i = this.getBaseMapper().receive(redEnvelope.getId(), uuid, receiveTime);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }

        RedEnvelopeSpilt redEnvelopeSpilt = this.getById(uuid);

        // 这个步骤会添加领取记录的缓存
        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord =
                redEnvelopeSpiltGetRecordService.redEnvelopeSpiltGetRecordFlow(uid, shortUid, uuid, redEnvelopeGetDTO, redEnvelopeSpilt);
        String coin = redEnvelope.getCoin();
        // 红包订单
        Order order = Order.builder()
                .uid(uid)
                .coin(coin)
                .orderNo(AccountChangeType.red_get.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(redEnvelopeSpilt.getAmount())
                .type(ChargeType.red_get) // 添加记录
                .completeTime(receiveTime)
                .createTime(receiveTime)
                .status(ChargeStatus.chain_success)
                // 关联领取订单详情
                .relatedId(redEnvelopeSpiltGetRecord.getId())
                .build();
        orderService.save(order);
        // 操作账户
        accountBalanceServiceImpl.increase(uid, ChargeType.red_get, coin, redEnvelopeSpilt.getAmount(), order.getOrderNo(), "抢红包获取");

        // 站外红包需要额外操作
        if (RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
            RedEnvelopeSpiltDTO redEnvelopeSpiltDTO = redEnvelopeConvert.toRedEnvelopeSpiltDTO(redEnvelopeSpilt);
            // 这个字段会导致修改失败，未领取的时候全部为false
            redEnvelopeSpiltDTO.setReceive(false);
            String oldMember = JSONUtil.toJsonStr(redEnvelopeSpiltDTO);
            redEnvelopeSpiltDTO.setReceive(true);
            String newMember = JSONUtil.toJsonStr(redEnvelopeSpiltDTO);

            String externKey = RedisConstants.RED_EXTERN + redEnvelope.getId(); //删除缓存 用于减少可领取兑换码缓存
            String externRecordKey = RedisConstants.RED_EXTERN_RECORD + redEnvelope.getId(); //修改缓存 用于更新领取信息

            stringRedisTemplate.opsForZSet().getOperations().executePipelined(new SessionCallback<>() {
                @Override
                public <K, V> Object execute(@Nonnull RedisOperations<K, V> operations) throws DataAccessException {

                    var zSetOperation = (ZSetOperations<String, String>) operations.opsForZSet();
                    zSetOperation.remove(externKey, oldMember);

                    zSetOperation.remove(externRecordKey, oldMember);
                    zSetOperation.add(externRecordKey, newMember, receiveTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    return null;
                }
            });
        }
        return redEnvelopeSpiltGetRecord;
    }

    @Override
    @Transactional
    public void spiltRedEnvelope(RedEnvelope redEnvelope) {
        List<RedEnvelopeSpilt> spiltRedEnvelopes = GiveStrategyAdapter.split(redEnvelope);
        this.saveBatch(spiltRedEnvelopes);

        RedEnvelopeChannel channel = redEnvelope.getChannel();

        if (!RedEnvelopeChannel.EXTERN.equals(channel)) {
            String key = RedisConstants.SPILT_RED_ENVELOPE + redEnvelope.getId();
            redisTemplate.opsForSet().add(key, spiltRedEnvelopes.stream().map(RedEnvelopeSpilt::getId).toArray());
            redisTemplate.expire(key, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);
        }

        // 如果是站外红包，设置 zset 缓存 （score 从0 开始）
        if (RedEnvelopeChannel.EXTERN.equals(channel)) {
            String externKey = RedisConstants.RED_EXTERN + redEnvelope.getId(); //设置缓存 用于获取兑换码
            String externRecordKey = RedisConstants.RED_EXTERN_RECORD + redEnvelope.getId(); //设置缓存 用于整合已经领取和未领取数据
            Set<ZSetOperations.TypedTuple<String>> typedTuples = new HashSet<>(spiltRedEnvelopes.size());
            for (int i = 0; i < spiltRedEnvelopes.size(); i++) {
                RedEnvelopeSpilt redEnvelopeSpilt = spiltRedEnvelopes.get(i);
                RedEnvelopeSpiltDTO redEnvelopeSpiltDTO = redEnvelopeConvert.toRedEnvelopeSpiltDTO(redEnvelopeSpilt);

                typedTuples.add(new DefaultTypedTuple<>(JSONUtil.toJsonStr(redEnvelopeSpiltDTO), (double) i));
            }

            stringRedisTemplate.opsForZSet().add(externKey, typedTuples);
            redisTemplate.expire(externKey, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);

            stringRedisTemplate.opsForZSet().add(externRecordKey, typedTuples);
            redisTemplate.expire(externRecordKey, redEnvelope.getChannel().getExpireDays(), TimeUnit.DAYS);
        }
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
        String externKey = RedisConstants.RED_EXTERN + rid; // 用于获取可领取code
        String exchangeCodeKey = RedisConstants.RED_EXTERN_CODE + uuid; // 设置缓存:验证码和红包信息对应
        String externRecordKey = RedisConstants.RED_EXTERN_RECORD + redEnvelope.getId(); //修改缓存 领取兑换码时更新信息
        String exchangeCodeExpireTime = "2";
        long now = System.currentTimeMillis();
        // 取出小于当前时间的红包，并且设置一个新的过期时间（当前时间 + 2小时）
        String script = "local key = KEYS[1]\n" +
                "local key2 = KEYS[2]\n" +
                "local key3 = KEYS[3]\n" +
                "local currentMs = tonumber(ARGV[1]) \n" +
                "local uuid = ARGV[2] \n" +
                "local termOfValidity =  tonumber(ARGV[3]) * 60 * 60 \n" +
                "if  redis.call('EXISTS', key) == 0 then\n" +
                "    return 'NOT_EXIST'\n" +
                "end\n" +
                "local spiltReds = redis.call('ZRANGEBYSCORE',key,0,currentMs,'LIMIT',0,1)\n" +
                "if spiltReds[1] == nil then\n" +
                "    return 'FINISH'\n" +
                "end\n" +
                "local score = currentMs + termOfValidity * 1000 \n" +
                "redis.call('ZADD',key,score,spiltReds[1])\n" +
                "redis.call('ZADD',key3,currentMs,spiltReds[1])\n" +
                "redis.call('SET',key2,spiltReds[1])\n" +
                "redis.call('EXPIRE',key2,termOfValidity)\n" +
                "return spiltReds[1]";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        String result = stringRedisTemplate.opsForValue().getOperations()
                .execute(redisScript, List.of(externKey, exchangeCodeKey, externRecordKey)
                        , String.valueOf(now), uuid.toString(), exchangeCodeExpireTime);
        if (StringUtils.isBlank(result)) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        if ("NOT_EXIST".equals(result)) {
            log.error("站外红包ZSET不存在:" + rid);
            ErrorCodeEnum.throwException("站外红包ZSET不存在");
        }
        if ("FINISH".equals(result)) {
            var typedTuples =
                    stringRedisTemplate.opsForZSet()
                            .rangeByScoreWithScores(externKey, now, Double.MAX_VALUE, 0, 1);
            if (CollectionUtils.isEmpty(typedTuples)) {
                return new RedEnvelopeExchangeCodeVO(RedEnvelopeStatus.FINISH);
            }

            var tuple = new ArrayList<>(typedTuples).get(0);
            Double score =
                    Optional.ofNullable(tuple.getScore()).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
            LocalDateTime dateTime =
                    LocalDateTime.ofEpochSecond(score.longValue() / 1000, 0, ZoneOffset.ofHours(8));
            return new RedEnvelopeExchangeCodeVO(RedEnvelopeStatus.FINISH_TEMP, dateTime);
        }
        RedEnvelopeSpilt redEnvelopeSpilt = JSONUtil.toBean(result, RedEnvelopeSpilt.class);
        return RedEnvelopeExchangeCodeVO.builder()
                .receiveAmount(redEnvelopeSpilt.getAmount())
                .exchangeCode(uuid.toString())
                .coin(redEnvelope.getCoin())
                .usdtRate(currencyService.huobiUsdtRate(redEnvelope.getCoin()))
                .usdtCnyRate(BigDecimal.valueOf(digitalCurrencyExchange.usdtCnyPrice()))
                .totalAmount(redEnvelope.getTotalAmount())
                .flag(redEnvelope.getFlag())
                .build();


    }

    @Override
    @SuppressWarnings("unchecked")
    public RedEnvelopeExternGetDetailsVO getExternDetailsRedis(RedEnvelope redEnvelope
            , PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {
        String coin = redEnvelope.getCoin();
        CoinBase coinBase = coinBaseService.getByName(coin);

        // 当前时间
        String externRecordKey = RedisConstants.RED_EXTERN_RECORD + redEnvelope.getId(); //获取缓存 用于获取列表

        // 已经领取过兑换码的数量
        Long receiveNum = MoreObjects.firstNonNull(
                stringRedisTemplate.opsForZSet().count(externRecordKey, TIME_BEGIN, Double.MAX_VALUE),
                0L);

        Set<ZSetOperations.TypedTuple<String>> recordsCache =
                Optional.ofNullable(stringRedisTemplate.opsForZSet().rangeByScoreWithScores(externRecordKey
                                , TIME_BEGIN, Double.MAX_VALUE, pageQuery.getOffset(), pageQuery.getPageSize()))
                        .orElse(SetUtils.EMPTY_SORTED_SET);


        List<RedEnvelopeExternGetRecordVO> redEnvelopeExternGetRecordVOS = recordsCache.stream().map(tuple -> {
            RedEnvelopeSpiltDTO dto = JSONUtil.toBean(tuple.getValue(), RedEnvelopeSpiltDTO.class);
            var score = Optional.ofNullable(tuple.getScore()).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

            return RedEnvelopeExternGetRecordVO.builder()
                    .amount(dto.getAmount())
                    .receive(dto.isReceive())
                    .receiveTime(LocalDateTime.ofEpochSecond(score.longValue() / 1000, 0, ZoneOffset.ofHours(8)))
                    .nickName("")
                    .headLogo("")
                    .build();
        }).collect(Collectors.toList());

        IPage<RedEnvelopeExternGetRecordVO> page = new Page<>();
        page.setTotal(receiveNum);
        page.setRecords(redEnvelopeExternGetRecordVOS);
        page.setSize(pageQuery.getPageSize());
        page.setCurrent(pageQuery.getPage());

        var vo = redEnvelopeConvert.toRedEnvelopeExternGetDetailsVO(redEnvelope);
        vo.setCoinUrl(coinBase.getLogo());
        vo.setUsdtRate(currencyService.getDollarRate(coin));
        vo.setExpireTime(redEnvelope.getCreateTime().plusDays(redEnvelope.getChannel().getExpireDays()));
        vo.setRecordPage(page);
        return vo;
    }

    @Override
    public RedEnvelopeSpiltDTO getRedEnvelopeSpiltDTOCache(String exchangeCode) {
        String exchangeCodeKey = RedisConstants.RED_EXTERN_CODE + exchangeCode; // 获取并删除:验证码和红包信息对应
        String cache = stringRedisTemplate.opsForValue().get(exchangeCodeKey);
        if (Objects.isNull(cache)) {
            ErrorCodeEnum.RED_EXCHANGE_ERROR.throwException();
        }
        stringRedisTemplate.delete(exchangeCodeKey);
        return JSONUtil.toBean(cache, RedEnvelopeSpiltDTO.class);
    }

}
