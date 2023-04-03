package com.tianli.accountred.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.accountred.convert.RedEnvelopeConvert;
import com.tianli.accountred.dto.RedEnvelopStatusDTO;
import com.tianli.accountred.dto.RedEnvelopeGetDTO;
import com.tianli.accountred.dto.RedEnvelopeSpiltDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.mapper.RedEnvelopeSpiltMapper;
import com.tianli.accountred.service.RedEnvelopeService;
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
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tron.tronj.utils.Numeric;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private static MessageDigest md5;

    private static final String PREFIX = "0x";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedEnvelopeSpiltGetRecordService redEnvelopeSpiltGetRecordService;
    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchange;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private RedEnvelopeConvert redEnvelopeConvert;
    @Resource
    private RedEnvelopeService redEnvelopeService;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    @Override
    @Transactional
    public RedEnvelopeSpiltGetRecord getSpilt(Long uid, Long shortUid, String spiltId, RedEnvelopeGetDTO redEnvelopeGetDTO) {

        LocalDateTime now = LocalDateTime.now();
        RedEnvelopeSpilt redEnvelopeSpilt = this.getById(spiltId);
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(redEnvelopeSpilt.getRid());
        RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord = redEnvelopeSpiltGetRecordService.getRecord(redEnvelope.getId(), uid);
        // 红包订单
        Order order = Order.builder()
                .uid(uid)
                .coin(redEnvelope.getCoin())
                .orderNo(AccountChangeType.red_get.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(redEnvelopeSpilt.getAmount())
                .type(ChargeType.red_get) // 添加记录
                .completeTime(now)
                .createTime(now)
                .status(ChargeStatus.chain_success)
                // 关联领取订单详情
                .relatedId(redEnvelopeSpiltGetRecord.getId())
                .build();
        orderService.save(order);
        // 操作账户
        accountBalanceService.increase(uid, ChargeType.red_get, redEnvelope.getCoin()
                , redEnvelopeSpilt.getAmount(), order.getOrderNo());
        String semaphore = RedisConstants.RED_SEMAPHORE + redEnvelope.getId() + ":" + uid;
        stringRedisTemplate.delete(semaphore);
        return redEnvelopeSpiltGetRecord;
    }

    @Override
    public RedEnvelopeSpiltGetRecord generateRecord(Long uid, Long shortUid, String spiltId, RedEnvelopeGetDTO redEnvelopeGetDTO) {
        LocalDateTime receiveTime = LocalDateTime.now();
        // 修改拆分（子）红包的状态
        int i = this.getBaseMapper().receive(redEnvelopeGetDTO.getRid(), spiltId, receiveTime);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }
        RedEnvelopeSpilt redEnvelopeSpilt = this.getById(spiltId);

        // 这个步骤会添加领取记录的缓存
        return redEnvelopeSpiltGetRecordService.redEnvelopeSpiltGetRecordFlow(uid, shortUid, spiltId, redEnvelopeGetDTO, redEnvelopeSpilt);
    }

    @Override
    @Transactional
    public void spiltRedEnvelope(RedEnvelope redEnvelope) {
        List<RedEnvelopeSpilt> spiltRedEnvelopes = GiveStrategyAdapter.split(redEnvelope);
        this.saveBatch(spiltRedEnvelopes);

        RedEnvelopeChannel channel = redEnvelope.getChannel();
        int expireDays = redEnvelope.getChannel().getExpireDays();
        if (!RedEnvelopeChannel.EXTERN.equals(channel)) {
            String key = RedisConstants.RED_CHAT + redEnvelope.getId();
            stringRedisTemplate.opsForSet().add(key, spiltRedEnvelopes.stream().map(RedEnvelopeSpilt::getId)
                    .toArray(String[]::new));
            stringRedisTemplate.expire(key, expireDays, TimeUnit.DAYS);
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
            redisTemplate.expire(externKey, expireDays, TimeUnit.DAYS);

            stringRedisTemplate.opsForZSet().add(externRecordKey, typedTuples);
            redisTemplate.expire(externRecordKey, expireDays, TimeUnit.DAYS);
        }
    }

    @Override
    public List<RedEnvelopeSpilt> getSpilt(Long rid, boolean receive) {
        return this.list(new LambdaQueryWrapper<RedEnvelopeSpilt>()
                .eq(RedEnvelopeSpilt::getRid, rid)
                .eq(RedEnvelopeSpilt::isReceive, receive));
    }

    @Override
    public RedEnvelopeExchangeCodeVO getExchangeCode(Long rid, String ip, String fingerprint) {

        RedEnvelope redEnvelope = Optional.ofNullable(redEnvelopeService.getWithCache(rid))
                .orElseThrow(ErrorCodeEnum.RED_NOT_EXIST::generalException);

        // 只支持站外红包
        if (!RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
            ErrorCodeEnum.RED_RECEIVE_NOT_ALLOW.throwException();
        }

        // 等待、失败、过期、结束
        if (!RedEnvelopeStatus.valid(redEnvelope.getStatus())) {
            return new RedEnvelopeExchangeCodeVO(redEnvelope.getStatus());
        }

        UUID uuidStr = UUID.randomUUID();
        String[] idd = uuidStr.toString().split("-");
        String uuid = (idd[0] + idd[1]).toUpperCase(Locale.ROOT);
        String externKey = RedisConstants.RED_EXTERN + rid; // 用于获取可领取code
        String exchangeCodeKey = RedisConstants.RED_EXTERN_CODE + uuid; // 设置缓存:验证码和红包信息对应
        String externRecordKey = RedisConstants.RED_EXTERN_RECORD + redEnvelope.getId(); //修改缓存 领取兑换码时更新信息
        String ipKey = RedisConstants.RED_ENVELOPE_LIMIT + ip; // 设置缓存
        String fingerprintKey = RedisConstants.RED_ENVELOPE_LIMIT + fingerprint; // 设置缓存
        String exchangeCodeExpireTime = "2";
        long now = System.currentTimeMillis();
        // 取出小于当前时间的红包，并且设置一个新的过期时间（当前时间 + 2小时）
        String script =
                "local rid = KEYS[6]\n" +
                        "local ipKey = KEYS[4]..':'..rid \n" +
                        "if redis.call('EXISTS',ipKey) ~= 0 then \n" +
                        "    local ipGetCount =  tonumber(redis.call('GET',ipKey)) \n" +
                        "    if ipGetCount >= 5 then \n" +
                        "        return 'IP_LIMIT' \n" +
                        "    end\n" +
                        "else \n" +
                        "    redis.call('SET',ipKey,tonumber(0)) \n" +
                        "    redis.call('EXPIRE',ipKey,30 * 24 * 60 * 60)\n" +
                        "end \n" +
                        "local externKey = KEYS[1]\n" +
                        "local exchangeCodeKey = KEYS[2]\n" +
                        "local externRecordKey = KEYS[3]\n" +
                        "local fingerprintKey = KEYS[5]..':'..rid \n" +
                        "local currentMs = tonumber(ARGV[1]) \n" +
                        "local uuid = ARGV[2] \n" +
                        "local termOfValidity =  tonumber(ARGV[3]) * 60 * 60 \n" +
                        "if  redis.call('EXISTS', externKey) == 0 then\n" +
                        "    return 'NOT_EXIST'\n" +
                        "end\n" +
                        "local spiltReds = redis.call('ZRANGEBYSCORE',externKey,0,currentMs,'LIMIT',0,1)\n" + //获取过期时间下（now）的记录
                        "if spiltReds[1] == nil then\n" +
                        "    return 'FINISH'\n" +
                        "end\n" +
                        "local score = currentMs + termOfValidity * 1000 \n" +
                        "redis.call('ZADD',externKey,score,spiltReds[1])\n" +  // 更新一下score
                        "redis.call('ZADD',externRecordKey,currentMs,spiltReds[1])\n" +
                        "local spiltRed = cjson.decode(spiltReds[1]) \n" +
                        "spiltRed.timestamp = score \n" +
                        "spiltRed.exchangeCode = uuid \n" +
                        "redis.call('SET',exchangeCodeKey,cjson.encode(spiltRed))\n" +
                        "redis.call('EXPIRE',exchangeCodeKey,termOfValidity)\n" +
                        "redis.call('SET',fingerprintKey,cjson.encode(spiltRed))\n" +
                        "redis.call('INCRBY',ipKey,tonumber(1))\n" +
                        "redis.call('EXPIRE',fingerprintKey,termOfValidity)\n" +
                        "return spiltReds[1]";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        String result = stringRedisTemplate.opsForValue().getOperations()
                .execute(redisScript, List.of(externKey, exchangeCodeKey, externRecordKey, ipKey, fingerprintKey, rid + "")
                        , String.valueOf(now), uuid, exchangeCodeExpireTime);
        if (StringUtils.isBlank(result)) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        if ("IP_LIMIT".equals(result)) {
            throw ErrorCodeEnum.RED_IP_LIMIT.generalException();
        }
        if ("NOT_EXIST".equals(result)) {
            log.error("站外红包ZSET不存在:" + rid);
            ErrorCodeEnum.throwException("站外红包ZSET不存在");
        }
        if ("FINISH".equals(result)) {
            RedEnvelopStatusDTO dto = getLatestExpireDTO(externKey, now);
            return new RedEnvelopeExchangeCodeVO(dto.getStatus(), dto.getLatestExpireTime());
        }

        RedEnvelopeSpilt redEnvelopeSpilt = JSONUtil.toBean(result, RedEnvelopeSpilt.class);
        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        return RedEnvelopeExchangeCodeVO.builder()
                .status(RedEnvelopeStatus.SUCCESS)
                .receiveAmount(redEnvelopeSpilt.getAmount())
                .exchangeCode(uuid)
                .coin(redEnvelope.getCoin())
                .usdtRate(currencyService.huobiUsdtRate(redEnvelope.getCoin()))
                .usdtCnyRate(BigDecimal.valueOf(digitalCurrencyExchange.usdtCnyPrice()))
                .totalAmount(redEnvelope.getTotalAmount())
                .flag(redEnvelope.getFlag())
                .coinUrl(coinBase.getLogo())
                .build();


    }

    public RedEnvelopStatusDTO getNotExpireDTO(String externKey, long now) {
        var typedTuples =
                stringRedisTemplate.opsForZSet()
                        .rangeByScoreWithScores(externKey, 0, now, 0, 1);
        if (CollectionUtils.isEmpty(typedTuples)) {
            return null;
        }

        return new RedEnvelopStatusDTO(RedEnvelopeStatus.PROCESS);
    }

    @Override
    public RedEnvelopStatusDTO getIpOrFingerDTO(String fingerprint, Long id) {
        String fingerprintKey = RedisConstants.RED_ENVELOPE_LIMIT + fingerprint + ":" + id;
        // EXCHANGE WAIT_EXCHANGE
        RedEnvelopeExchangeCodeVO vo;
        if ((vo = this.getInfo(fingerprintKey)) != null) {
            RedEnvelopeSpilt redEnvelopeSpilt = this.getById(vo.getSpiltRid());

            RedEnvelopStatusDTO redEnvelopStatusDTO = JSONUtil.parse(vo).toBean(RedEnvelopStatusDTO.class);

            redEnvelopStatusDTO.setStatus(redEnvelopeSpilt.isReceive() ? RedEnvelopeStatus.EXCHANGE
                    : RedEnvelopeStatus.WAIT_EXCHANGE);
            return redEnvelopStatusDTO;
        }
        return null;
    }

    public RedEnvelopStatusDTO getLatestExpireDTO(String externKey, long now) {
        var typedTuples =
                stringRedisTemplate.opsForZSet()
                        .rangeByScoreWithScores(externKey, now, Double.MAX_VALUE, 0, 1);
        if (CollectionUtils.isEmpty(typedTuples)) {
            return new RedEnvelopStatusDTO(RedEnvelopeStatus.FINISH);
        }

        var tuple = new ArrayList<>(typedTuples).get(0);
        Double score =
                Optional.ofNullable(tuple.getScore()).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
        LocalDateTime dateTime =
                LocalDateTime.ofEpochSecond(score.longValue() / 1000, 0, ZoneOffset.ofHours(8));
        return new RedEnvelopStatusDTO(RedEnvelopeStatus.FINISH_TEMP, dateTime);
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
                Optional.ofNullable(stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(externRecordKey
                                , TIME_BEGIN, Double.MAX_VALUE, pageQuery.getOffset(), pageQuery.getPageSize()))
                        .orElse(SetUtils.EMPTY_SORTED_SET);


        List<RedEnvelopeExternGetRecordVO> redEnvelopeExternGetRecordVOS = recordsCache.stream().map(tuple -> {
            RedEnvelopeSpiltDTO dto = JSONUtil.toBean(tuple.getValue(), RedEnvelopeSpiltDTO.class);
            var score = Optional.ofNullable(tuple.getScore()).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

            return RedEnvelopeExternGetRecordVO.builder()
                    .amount(dto.getAmount())
                    .receive(dto.isReceive())
                    .receiveTime(LocalDateTime.ofEpochSecond(score.longValue() / 1000, 0, ZoneOffset.ofHours(8)))
                    .nickName(
                            PREFIX + Numeric.toHexString((md5.digest((tuple.getScore().longValue() + "")
                                    .getBytes(StandardCharsets.UTF_8)))).substring(4, 10)
                    )
                    .coin(coin)
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
        String exchangeCodeKey = RedisConstants.RED_EXTERN_CODE + exchangeCode;
        String cache = stringRedisTemplate.opsForValue().get(exchangeCodeKey);
        if (Objects.isNull(cache)) {
            ErrorCodeEnum.RED_EXCHANGE_ERROR.throwException();
        }
        return JSONUtil.toBean(cache, RedEnvelopeSpiltDTO.class);
    }

    private RedEnvelopeExchangeCodeVO getInfo(String key) {
        String cache = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(cache)) {
            return null;
        }
        Long timestamp = JSONUtil.parse(cache).getByPath("timestamp", Long.class);
        String exchangeCode = JSONUtil.parse(cache).getByPath("exchangeCode", String.class);
        RedEnvelopeSpiltDTO dto = JSONUtil.toBean(cache, RedEnvelopeSpiltDTO.class);
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.valueOf(dto.getRid()));
        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        return RedEnvelopeExchangeCodeVO.builder()
                .receiveAmount(dto.getAmount())
                .exchangeCode(exchangeCode)
                .coin(redEnvelope.getCoin())
                .usdtRate(currencyService.huobiUsdtRate(redEnvelope.getCoin()))
                .usdtCnyRate(BigDecimal.valueOf(digitalCurrencyExchange.usdtCnyPrice()))
                .totalAmount(redEnvelope.getTotalAmount())
                .flag(redEnvelope.getFlag())
                .spiltRid(dto.getId())
                .coinUrl(coinBase.getLogo())
                .latestExpireTime(LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.ofHours(8)))
                .build();
    }
}
