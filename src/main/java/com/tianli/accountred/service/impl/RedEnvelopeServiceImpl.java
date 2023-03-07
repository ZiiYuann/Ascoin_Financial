package com.tianli.accountred.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.accountred.RedEnvelopeVerifier;
import com.tianli.accountred.convert.RedEnvelopeConvert;
import com.tianli.accountred.dto.RedEnvelopeGetDTO;
import com.tianli.accountred.dto.RedEnvelopeSpiltDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.accountred.enums.RedEnvelopeWay;
import com.tianli.accountred.mapper.RedEnvelopeMapper;
import com.tianli.accountred.query.*;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.*;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.CustomException;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsService;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.RedEnvelopeContext;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.crypto.PBE;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianli.common.ConfigConstants.SYSTEM_URL_PATH_PREFIX;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Slf4j
@Service
public class RedEnvelopeServiceImpl extends ServiceImpl<RedEnvelopeMapper, RedEnvelope> implements RedEnvelopeService {

    public static final String BLOOM = "bloom";

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private RedEnvelopeConvert redEnvelopeConvert;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;
    @Resource
    private RedEnvelopeSpiltGetRecordService redEnvelopeSpiltGetRecordService;
    @Resource
    private OrderService orderService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private SqlSession sqlSession;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedEnvelopeMapper redEnvelopeMapper;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private RedEnvelopeConfigService redEnvelopeConfigService;
    @Resource
    private SqsService sqsService;

    private final List<RedEnvelopeVerifier> verifiers = new ArrayList<>();

    @PostConstruct
    public void initBloomFilter() {
        // 布隆过滤器，防止缓存击穿
        final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + BLOOM);

        // 修改红包序列化
        List<RedEnvelope> redEnvelopes = redEnvelopeMapper.selectList(new LambdaQueryWrapper<RedEnvelope>()
                .select(RedEnvelope::getId)
                .ge(RedEnvelope::getCreateTime, LocalDateTime.now().plusHours(-25)));
        List<String> deleteKeys = redEnvelopes.stream().map(redEnvelope -> RedisConstants.RED_ENVELOPE + redEnvelope.getId())
                .collect(Collectors.toList());
        stringRedisTemplate.delete(deleteKeys);

        if (bloomFilter.isExists()) {
            return;
        }

        log.info("初始化布隆过滤器【red:bloom】");
        bloomFilter.tryInit(10000000L, 0.001f);
        final List<Long> ids = redEnvelopeMapper.listIds();
        ids.forEach(bloomFilter::add);
    }

    @PostConstruct
    public void initRedEnvelopeVerifier() {
        verifiers.add(new ChatVerifier());
        verifiers.add(new InviteVerifier());
    }


    @Override
    @Transactional
    public Result give(Long uid, Long shortUid, RedEnvelopeIoUQuery query) {

        boolean walletWay = RedEnvelopeWay.WALLET.equals(query.getWay());
        // 生成红包
        RedEnvelope redEnvelope = redEnvelopeConvert.toDO(query);
        redEnvelope.setTotalAmount(query.getTotalAmount());
        redEnvelope.setCreateTime(LocalDateTime.now());
        redEnvelope.setId(CommonFunction.generalId());
        redEnvelope.setStatus(walletWay ? RedEnvelopeStatus.PROCESS : RedEnvelopeStatus.WAIT);
        redEnvelope.setUid(uid);
        redEnvelope.setShortUid(shortUid);
        this.save(redEnvelope);

        if (walletWay) {
            spiltRedEnvelope(uid, redEnvelope);
        }

        setBloomCache(redEnvelope.getId());
        setRedisCache(redEnvelope);
        return Result.success(new RedEnvelopeGiveVO(redEnvelope.getId()
                , redEnvelope.getChannel()
                , getExternUrl(redEnvelope.getChannel(), redEnvelope.getId())));
    }

    /**
     * 具体拆分红包
     *
     * @param uid         uid
     * @param redEnvelope 红包
     */
    private void spiltRedEnvelope(Long uid, RedEnvelope redEnvelope) {

        // 责任链模式校验
        verifiers.forEach(verifier -> verifier.verifier(uid, redEnvelope));

        // 拆分红包
        redEnvelopeSpiltService.spiltRedEnvelope(redEnvelope);

        // 红包订单
        Order order = Order.builder()
                .uid(uid)
                .coin(redEnvelope.getCoin())
                .orderNo(AccountChangeType.red_give.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(redEnvelope.getTotalAmount())
                .type(ChargeType.red_give)
                .completeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .status(ChargeStatus.chain_success)
                .relatedId(redEnvelope.getId())
                .build();
        orderService.save(order);

        // 直接扣除红包金额
        accountBalanceService.decrease(uid, ChargeType.red_give, redEnvelope.getCoin(), redEnvelope.getTotalAmount()
                , order.getOrderNo());
    }

    @Override
    @SneakyThrows
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Result<RedEnvelopeGiveVO> give(Long uid, Long shortUid, RedEnvelopeChainQuery query) {
        RedEnvelope redEnvelope = this.getWithCache(query.getId());
        redEnvelope = Optional.ofNullable(redEnvelope).orElseThrow(ErrorCodeEnum.RED_NOT_EXIST::generalException);

        redEnvelope.setTxid(query.getTxid());
        sqlSession.clearCache();
        Order order = orderService.getOrderByHash(query.getTxid(), ChargeType.recharge);

        // 判断订单状态
        if (Objects.isNull(order) || !ChargeType.recharge.equals(order.getType()) ||
                !ChargeStatus.chain_success.equals(order.getStatus())) {
            return Result.fail(ErrorCodeEnum.RED_ORDER_NOT_FIND);
        }

        if (order.getAmount().compareTo(redEnvelope.getTotalAmount()) != 0 || !order.getCoin().equals(redEnvelope.getCoin())) {
            redEnvelope.setStatus(RedEnvelopeStatus.FAIL);
            redEnvelope.setFinishTime(LocalDateTime.now());
            this.saveOrUpdate(redEnvelope);
            return Result.fail("红包发送失败，充值金额与红包金额不一致或者币别不一致");
        }

        if (!order.getUid().equals(redEnvelope.getUid())) {
            redEnvelope.setStatus(RedEnvelopeStatus.FAIL);
            redEnvelope.setFinishTime(LocalDateTime.now());
            this.saveOrUpdate(redEnvelope);
            return Result.fail("充值订单用户和充值用户不一致");
        }

        redEnvelope.setStatus(RedEnvelopeStatus.PROCESS);
        this.spiltRedEnvelope(uid, redEnvelope);
        this.process(redEnvelope.getId(), query.getTxid());

        setRedisCache(redEnvelope);
        return Result.success(new RedEnvelopeGiveVO(redEnvelope.getId()
                , redEnvelope.getChannel()
                , getExternUrl(redEnvelope.getChannel(), redEnvelope.getId())));
    }

    @Override
    @SneakyThrows
    public RedEnvelopeGetVO get(Long uid, Long shortUid, RedEnvelopeGetQuery query) {
        // 判断红包缓存是否存在
        RedEnvelope redEnvelope = Optional.ofNullable(this.getWithCache(query.getRid()))
                .orElseThrow(ErrorCodeEnum.RED_NOT_EXIST::generalException);

        // 站外红包不支持此领取方式
        if (RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
            ErrorCodeEnum.RED_RECEIVE_NOT_ALLOW.throwException();
        }

        if (!redEnvelope.getFlag().equals(query.getFlag())) {
            webHookService.dingTalkSend("FLAG不一致！领取FLAG：" + query.getFlag() + "  红包FLAG：" + redEnvelope.getFlag());
            ErrorCodeEnum.RED_RECEIVE_NOT_ALLOW.throwException();
        }

        // 如果是私聊红包，判断领取对象和红包标示是否一致
        if (RedEnvelopeType.PRIVATE.equals(redEnvelope.getType()) && !String.valueOf(shortUid).equals(redEnvelope.getFlag())) {
            webHookService.dingTalkSend("ID不一致！领取红包用户ID：" + shortUid + "  与标识用户ID：" + redEnvelope.getFlag());
            ErrorCodeEnum.RED_RECEIVE_NOT_ALLOW.throwException();
        }

        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        // 等待、失败、过期、结束
        if (!RedEnvelopeStatus.valid(redEnvelope.getStatus())) {
            return new RedEnvelopeGetVO(redEnvelope.getStatus(), coinBase);
        }

        query.setRedEnvelope(redEnvelope);

        RedEnvelopeGetVO operation = this.getByChatOperation(uid, shortUid, query, redEnvelope);

        // 缓存延时双删
        Thread.sleep(100);
        this.deleteRedisCache(query.getRid());
        return operation;
    }

    @Override
    public RedEnvelopeGetVO get(Long uid, Long shortUid, RedEnvelopeExchangeCodeQuery query) {
        RedEnvelopeSpiltDTO redEnvelopeSpiltDTO =
                redEnvelopeSpiltService.getRedEnvelopeSpiltDTOCache(query.getExchangeCode());

        if (redEnvelopeSpiltDTO.isReceive()) {
            ErrorCodeEnum.RED_HAVE_RECEIVED.throwException();
        }

        Long rid = Long.valueOf(redEnvelopeSpiltDTO.getRid());
        RedEnvelope redEnvelope = this.getById(rid);

        // 不是站外红包不支持此领取方式
        if (!RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
            ErrorCodeEnum.RED_RECEIVE_NOT_ALLOW.throwException();
        }

        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());

        RedEnvelopeStatus status = redEnvelope.getStatus();
        // 等待、失败、过期、结束
        if (!RedEnvelopeStatus.valid(status)) {
            ErrorCodeEnum.RED_OVERDUE.throwException();
        }

        // 这个字段会导致修改失败，未领取的时候全部为false
        String oldMember = JSONUtil.toJsonStr(redEnvelopeSpiltDTO);
        redEnvelopeSpiltDTO.setReceive(true);
        String newMember = JSONUtil.toJsonStr(redEnvelopeSpiltDTO);
        String newMemberScore = LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() + "";

        String externRecordKey = RedisConstants.RED_EXTERN_RECORD + redEnvelope.getId(); //修改缓存 用于更新领取信息
        String receivedUidKey = RedisConstants.SPILT_RED_ENVELOPE_GET + rid + ":" + uid;
        String receivedDeviceKey = RedisConstants.SPILT_RED_ENVELOPE_GET + rid + ":" + query.getDeviceNumber();
        String exchangeCodeKey = RedisConstants.RED_EXTERN_CODE + query.getExchangeCode();
        String semaphore = RedisConstants.RED_SEMAPHORE + rid + ":" + uid;
        String externKey = RedisConstants.RED_EXTERN + redEnvelope.getId(); //删除缓存 用于减少可领取兑换码缓存

        String script =
                "local externKey = KEYS[4] \n" +
                        "local externRecordKey = KEYS[5] \n" +
                        "local receivedDeviceKey = KEYS[6] \n" +
                        "local oldMember = ARGV[1] \n" +
                        "local newMember = ARGV[2] \n" +
                        "local newMemberScore = ARGV[3] \n" +
                        "if redis.call('EXISTS', KEYS[1]) > 0 then\n" +
                        "    return 'RECEIVED'\n" +
                        "elseif redis.call('EXISTS', receivedDeviceKey) > 0 then\n" +
                        "    return 'RECEIVED'\n" +
                        "elseif redis.call('EXISTS', KEYS[2]) == 0 then\n" +
                        "    return 'FINISH'\n" +
                        "else\n" +
                        "    redis.call('SET',KEYS[1],'')\n" +
                        "    redis.call('EXPIRE',KEYS[1],2592000)\n" +
                        "    redis.call('SET',receivedDeviceKey,'')\n" +
                        "    redis.call('EXPIRE',receivedDeviceKey,2592000)\n" +
                        "    redis.call('DEL',KEYS[2])\n" +
                        "    redis.call('SET',KEYS[3],'')\n" +
                        "    redis.call('ZREM',externKey,oldMember)\n" +
                        "    redis.call('ZREM',externRecordKey,oldMember)\n" +
                        "    redis.call('ZADD',externRecordKey,newMemberScore,newMember)\n" +
                        "end\n";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        String result;
        try {
            result = stringRedisTemplate.opsForValue().getOperations().execute(redisScript
                    , List.of(receivedUidKey, exchangeCodeKey, semaphore, externKey, externRecordKey, receivedDeviceKey)
                    , oldMember, newMember, newMemberScore);
        } catch (Exception e) {
            // 防止由于网络波动导致的红包异常 暂时做通知处理，如果情况比较多，后续进行补偿
            webHookService.dingTalkSend("兑换红包状态异常，请及时处理，红包id：" + rid + " uid：" + uid, e);
            throw e;
        }

        if ((status = EnumUtils.getEnum(RedEnvelopeStatus.class, result)) != null) {
            return new RedEnvelopeGetVO(status, coinBase);
        }

        RedEnvelopeGetDTO dto = RedEnvelopeGetDTO.builder().deviceNumber(query.getDeviceNumber())
                .redEnvelope(redEnvelope)
                .rid(redEnvelope.getId())
                .exchangeCode(query.getExchangeCode())
                .build();
        redEnvelopeSpiltService.generateRecord(uid, shortUid, redEnvelopeSpiltDTO.getId(), dto);

        // 异步转账
        RedEnvelopeContext redEnvelopeContext = RedEnvelopeContext.builder()
                .rid(redEnvelope.getId())
                .uuid(redEnvelopeSpiltDTO.getId())
                .uid(uid)
                .shortUid(shortUid)
                .deviceNumber(query.getDeviceNumber())
                .exchangeCode(query.getExchangeCode())
                .build();
        sqsService.send(new SqsContext<>(SqsTypeEnum.RED_ENVELOP, redEnvelopeContext)); // 站外红包异步

        RedEnvelopeGetVO redEnvelopeGetVO = new RedEnvelopeGetVO(RedEnvelopeStatus.SUCCESS, coinBase);
        redEnvelopeGetVO.setReceiveAmount(redEnvelopeSpiltDTO.getAmount());
        redEnvelopeGetVO.setUReceiveAmount(currencyService.getDollarRate(redEnvelope.getCoin()).multiply(redEnvelopeSpiltDTO.getAmount()));
        redEnvelopeGetVO.setUid(redEnvelope.getUid());
        redEnvelopeGetVO.setShortUid(redEnvelope.getShortUid());
        redEnvelopeGetVO.setRemarks(redEnvelope.getRemarks());
        return redEnvelopeGetVO;
    }

    @Override
    public RedEnvelopeGetDetailsVO getDetails(Long uid, Long rid) {
        RedEnvelope redEnvelope = getWithCache(rid);
        List<RedEnvelopeSpiltGetRecordVO> recordVo = redEnvelopeSpiltGetRecordService.getRecordVos(redEnvelope);

        RedEnvelopeGetDetailsVO redEnvelopeGetDetailsVO = redEnvelopeConvert.toRedEnvelopeGetDetailsVO(redEnvelope);
        redEnvelopeGetDetailsVO.setRecords(recordVo);
        redEnvelopeGetDetailsVO.setReceiveNum(recordVo.size());

        RedEnvelopeSpiltGetRecord getRecord = redEnvelopeSpiltGetRecordService.getRecord(rid, uid);
        redEnvelopeGetDetailsVO.setReceiveAmount(Objects.isNull(getRecord) ? null : getRecord.getAmount());
        redEnvelopeGetDetailsVO.setUReceiveAmount(Objects.isNull(getRecord) ? null
                : getRecord.getAmount().multiply(currencyService.getDollarRate(redEnvelope.getCoin())));

        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        redEnvelopeGetDetailsVO.setCoinUrl(coinBase.getLogo());
        return redEnvelopeGetDetailsVO;
    }


    /**
     * 聊天抢红包（纯redis和MySQL查询操作，不需要事务）
     *
     * @param uid         抢红包uid
     * @param shortUid    抢红包用户短码
     * @param query       请求参数
     * @param redEnvelope 红包信息
     * @return 返回信息
     */
    private RedEnvelopeGetVO getByChatOperation(Long uid, Long shortUid, RedEnvelopeGetQuery query, RedEnvelope redEnvelope) {

        String receivedKey = RedisConstants.SPILT_RED_ENVELOPE_GET + query.getRid() + ":" + uid;
        String spiltRedKey = RedisConstants.RED_CHAT + query.getRid();
        String semaphore = RedisConstants.RED_SEMAPHORE + query.getRid() + ":" + uid;
        // 此lua脚本的用处 1、判断用户是否已经抢过红包 2、判断拆分红包是否还有剩余
        String script =
                "if redis.call('EXISTS', KEYS[1]) > 0 then\n" +
                        "    return 'RECEIVED'\n" +
                        "elseif redis.call('EXISTS', KEYS[2]) == 0 then\n" +
                        "    return 'FINISH'\n" +
                        "else\n" +
                        "    redis.call('SET',KEYS[1],'')\n" +
                        "    redis.call('EXPIRE',KEYS[1],86400)\n" +
                        "    redis.call('SET',KEYS[3],'')\n" +
                        "    return redis.call('SPOP', KEYS[2])\n" +
                        "end";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        String result;
        try {
            result = stringRedisTemplate.opsForValue().getOperations().execute(redisScript
                    , List.of(receivedKey, spiltRedKey, semaphore));
        } catch (Exception e) {
            // 防止由于网络波动导致的红包异常 暂时做通知处理，如果情况比较多，后续进行补偿
            webHookService.dingTalkSend("领取红包状态异常，请及时处理，红包id：" + query.getRid() + " uid：" + uid, e);
            throw e;
        }

        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        RedEnvelopeStatus status;

        if ((status = EnumUtils.getEnum(RedEnvelopeStatus.class, result)) != null) {
            return new RedEnvelopeGetVO(status, coinBase);
        }

        RedEnvelopeGetDTO dto = RedEnvelopeGetDTO.builder().deviceNumber(query.getDeviceNumber())
                .redEnvelope(redEnvelope)
                .rid(redEnvelope.getId())
                .exchangeCode(null)
                .build();
        redEnvelopeSpiltService.generateRecord(uid, shortUid, result, dto);

        // 异步转账
        RedEnvelopeContext redEnvelopeContext = RedEnvelopeContext.builder()
                .rid(query.getRid())
                .uuid(result)
                .uid(uid)
                .shortUid(shortUid)
                .deviceNumber(query.getDeviceNumber())
                .build();
        sqsService.send(new SqsContext<>(SqsTypeEnum.RED_ENVELOP, redEnvelopeContext)); // 聊天红包异步

        var redEnvelopeSpilt = redEnvelopeSpiltService.getById(result);
        RedEnvelopeGetVO redEnvelopeGetVO = new RedEnvelopeGetVO(RedEnvelopeStatus.SUCCESS, coinBase);
        redEnvelopeGetVO.setReceiveAmount(redEnvelopeSpilt.getAmount());
        redEnvelopeGetVO.setUReceiveAmount(currencyService.getDollarRate(redEnvelope.getCoin()).multiply(redEnvelopeSpilt.getAmount()));
        redEnvelopeGetVO.setUid(redEnvelope.getUid());
        redEnvelopeGetVO.setShortUid(redEnvelope.getShortUid());
        return redEnvelopeGetVO;
    }

    @Override
    public IPage<RedEnvelopeGiveRecordVO> giveRecord(RedEnvelopeGiveRecordQuery query, PageQuery<RedEnvelope> pageQuery) {
        LambdaQueryWrapper<RedEnvelope> queryWrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(query.getUid())) {
            queryWrapper = queryWrapper.eq(RedEnvelope::getUid, query.getUid());
        }

        if (Objects.nonNull(query.getCreateTime())) {
            queryWrapper = queryWrapper.eq(RedEnvelope::getCreateTime, query.getCreateTime());
        }

        if (Objects.nonNull(query.getChannel())) {
            queryWrapper = queryWrapper.eq(RedEnvelope::getChannel, query.getChannel());
        }

        if (Objects.isNull(query.getStatus())) {
            queryWrapper = queryWrapper.ne(RedEnvelope::getStatus, RedEnvelopeStatus.WAIT);
        } else {
            queryWrapper = queryWrapper.eq(RedEnvelope::getStatus, query.getStatus());
        }

        queryWrapper = queryWrapper.last(" order by create_time desc ");

        Page<RedEnvelope> page = this.page(pageQuery.page(), queryWrapper);
        return page.convert(red -> {
            var vo = redEnvelopeConvert.toRedEnvelopeGiveRecordVO(red);
            vo.setExternUrl(this.getExternUrl(red.getChannel(), red.getId()));
            vo.setCanBack(RedEnvelopeChannel.EXTERN.equals(red.getChannel())
                    && LocalDateTime.now().compareTo(red.getCreateTime().plusDays(1)) >= 0
                    && RedEnvelopeStatus.PROCESS.equals(red.getStatus()));
            vo.setNotReceiveAmount(red.getTotalAmount().subtract(red.getReceiveAmount()));
            return vo;
        });
    }

    @Override
    @Transactional
    public void expireRed(LocalDateTime now) {
        // 2022-10-10 13:00:00 创建 如果当前时间是 2022-10-11 14:00:00（-1  2022-10-10 14:00:00）
        // plusMinutes(1) 等待缓存过期，确保过期操作内不会有人拿红包
        LocalDateTime chatExpireDate = now.plusDays(-RedEnvelopeChannel.CHAT.getExpireDays()).plusMinutes(1);
        LambdaQueryWrapper<RedEnvelope> queryWrapper1 = new LambdaQueryWrapper<RedEnvelope>()
                .eq(RedEnvelope::getStatus, RedEnvelopeStatus.PROCESS)
                .eq(RedEnvelope::getChannel, RedEnvelopeChannel.CHAT)
                .lt(RedEnvelope::getCreateTime, chatExpireDate);
        List<RedEnvelope> chatRedEnvelopes = this.list(queryWrapper1);

        LocalDateTime externExpireDate = now.plusDays(-30).plusMinutes(1);
        LambdaQueryWrapper<RedEnvelope> queryWrapper2 = new LambdaQueryWrapper<RedEnvelope>()
                .eq(RedEnvelope::getStatus, RedEnvelopeStatus.PROCESS)
                .eq(RedEnvelope::getChannel, RedEnvelopeChannel.EXTERN)
                .lt(RedEnvelope::getCreateTime, externExpireDate);
        List<RedEnvelope> externRedEnvelopes = this.list(queryWrapper2);

        ArrayList<RedEnvelope> redEnvelopes = new ArrayList<>();
        redEnvelopes.addAll(chatRedEnvelopes);
        redEnvelopes.addAll(externRedEnvelopes);

        if (CollectionUtils.isNotEmpty(redEnvelopes)) {
            redEnvelopes.forEach(redEnvelope -> {
                try {
                    RedEnvelopeServiceImpl bean = ApplicationContextTool.getBean(RedEnvelopeServiceImpl.class);
                    if (Objects.isNull(bean)) {
                        ErrorCodeEnum.SYSTEM_ERROR.throwException();
                    }
                    bean.redEnvelopeRollback(redEnvelope, RedEnvelopeStatus.OVERDUE);
                } catch (Exception e) {
                    webHookService.dingTalkSend("红包到期回滚异常：" + redEnvelope.getId(), e);
                }
            });
        }

    }

    @Override
    public RedEnvelopeGetVO getInfoById(Long id) {
        RedEnvelope redEnvelope = this.getWithCache(id);
        RedEnvelopeGetVO redEnvelopeGetVO = new RedEnvelopeGetVO();
        redEnvelopeGetVO.setStatus(redEnvelope.getStatus());
        redEnvelopeGetVO.setCoin(redEnvelope.getCoin());
        return redEnvelopeGetVO;
    }

    @Override
    @Transactional
    public void asynGet(RedEnvelopeContext sqsContext) {
        Long rid = sqsContext.getRid();
        String spiltId = sqsContext.getUuid();
        Long uid = sqsContext.getUid();
        Long shortUid = sqsContext.getShortUid();
        String deviceNumber = sqsContext.getDeviceNumber();

        RedEnvelope redEnvelope = this.getWithCache(rid);

        RedEnvelopeGetDTO query = RedEnvelopeGetDTO.builder().deviceNumber(deviceNumber)
                .redEnvelope(redEnvelope)
                .rid(rid)
                .exchangeCode(sqsContext.getExchangeCode())
                .build();

        // 领取子红包（创建订单，余额操作）
        var redEnvelopeSpilt = redEnvelopeSpiltService.getSpilt(uid, shortUid, spiltId, query);
        // 增加已经领取红包个数
        int i = this.getBaseMapper().increaseReceive(query.getRid(), redEnvelopeSpilt.getAmount());
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
            return;
        }
        // 如果红包领取完毕则修改红包状态
        redEnvelope = this.getById(query.getRid());
        if (redEnvelope.getNum() == redEnvelope.getReceiveNum()) {
            this.finish(query.getRid(), LocalDateTime.now());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void redEnvelopeRollback(RedEnvelope redEnvelope, RedEnvelopeStatus status) {
        String semaphore = RedisConstants.RED_SEMAPHORE + redEnvelope.getId();
        Set<String> keys = stringRedisTemplate.keys(semaphore + "*");
        if (CollectionUtils.isNotEmpty(keys)) {
            ErrorCodeEnum.RED_TRANSFER_ING.throwException();
        }

        List<RedEnvelopeSpilt> spiltRedEnvelopes =
                redEnvelopeSpiltService.getSpilt(redEnvelope.getId(), false);

        // 进入这个方法的红包应该存在红包未领取
        if (RedEnvelopeStatus.OVERDUE.equals(status)
                && RedEnvelopeChannel.CHAT.equals(redEnvelope.getChannel())
                && CollectionUtils.isEmpty(spiltRedEnvelopes)) {
            webHookService.dingTalkSend("红包状态不为领取完，但是拆分红包不存在，请排查异常：" + redEnvelope.getId());
            return;
        }

        int noReceiveNum = redEnvelope.getNum() - redEnvelope.getReceiveNum();
        if (spiltRedEnvelopes.size() != noReceiveNum) {
            webHookService.dingTalkSend("未领取红包数量与回滚数量不一致：" + redEnvelope.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal rollbackAmount = spiltRedEnvelopes.stream().map(RedEnvelopeSpilt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 红包回滚订单
        Order order = Order.builder()
                .uid(redEnvelope.getUid())
                .coin(redEnvelope.getCoin())
                .orderNo(AccountChangeType.red_back.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(rollbackAmount)
                .type(ChargeType.red_back)
                .completeTime(now)
                .createTime(now)
                .status(ChargeStatus.chain_success)
                .relatedId(redEnvelope.getId())
                .build();
        orderService.save(order);

        accountBalanceService.increase(redEnvelope.getUid(), ChargeType.red_back, redEnvelope.getCoin(), rollbackAmount
                , order.getOrderNo());


        this.statusProcess(redEnvelope.getId(), status, redEnvelope.getReceiveNum(), now);

        this.deleteRedisCache(redEnvelope.getId());
    }


    /**
     * 获取红包信息（缓存）
     * 1、发红包（上链）
     * 2、抢红包
     * 3、红包详情
     * 4、单独调用查询红包状态
     */
    @Override
    public RedEnvelope getWithCache(Long id) {
        // 布隆过滤器，防止缓存击穿
        final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + BLOOM);
        if (!bloomFilter.contains(id)) {
            ErrorCodeEnum.RED_NOT_EXIST_BLOOM.throwException();
        }

        var cache = stringRedisTemplate.opsForValue().get(RedisConstants.RED_ENVELOPE + id); // 获取缓存
        if (Objects.isNull(cache)) {
            RedEnvelope redEnvelope = this.getById(id);
            if (Objects.isNull(redEnvelope)) {
                throw ErrorCodeEnum.RED_NOT_EXIST.generalException();
            }
            setRedisCache(redEnvelope);
            return redEnvelope;
        }
        return JSONUtil.toBean(cache, RedEnvelope.class);
    }

    @Override
    public RedEnvelope getWithCache(Long uid, Long id) {
        RedEnvelope redEnvelope = getWithCache(id);
        if (!redEnvelope.getUid().equals(uid)) {
            ErrorCodeEnum.RED_NOT_EXIST.throwException();
        }

        return redEnvelope;
    }

    /**
     * 将进行中状态进行修改
     *
     * @param status     状态
     * @param receiveNum 已经领取数量
     */
    private void statusProcess(Long id, RedEnvelopeStatus status, int receiveNum, LocalDateTime now) {
        int i = this.getBaseMapper().statusProcess(id, status, receiveNum, now);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }
    }

    /**
     * 把红包状态设置为结束
     */
    private void finish(Long id, LocalDateTime finishTime) {
        int i = this.getBaseMapper().finish(id, finishTime);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }
    }

    /**
     * 把红包状态设置为进行中
     */
    private void process(Long id, String txid) {
        int i = this.getBaseMapper().process(id, txid);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }
    }

    /**
     * 删除红包缓存
     */
    @Override
    public void deleteRedisCache(Long id) {
        redisTemplate.delete(RedisConstants.RED_ENVELOPE + id); // 删除缓存
    }

    @Override
    @Transactional
    public void backRed(Long uid, Long rid) {
        RedEnvelope redEnvelope = getWithCache(uid, rid);
        // 只支持站外红包
        if (!RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
            ErrorCodeEnum.RED_RECEIVE_NOT_ALLOW.throwException();
        }

        RedEnvelopeServiceImpl bean = ApplicationContextTool.getBean(RedEnvelopeServiceImpl.class);
        if (Objects.isNull(bean)) {
            throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
        }

        // 如果不进行此操作，还可以继续领取验证码
        String externKey = RedisConstants.RED_EXTERN + redEnvelope.getId(); //删除缓存 用于减少可领取兑换码缓存
        stringRedisTemplate.delete(externKey);

        bean.redEnvelopeRollback(redEnvelope, RedEnvelopeStatus.BACK);
    }

    /**
     * 增加红包缓存
     * 1、发红包本地
     * 2、发红包链上
     * 3、getWithCache
     */
    private void setRedisCache(RedEnvelope redEnvelope) {
        String key = RedisConstants.RED_ENVELOPE + redEnvelope.getId(); // 设置缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redEnvelope), 1L, TimeUnit.DAYS);
    }

    /**
     * 设置步隆过滤器缓存
     */
    private void setBloomCache(Long id) {
        final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + BLOOM);
        boolean success = bloomFilter.add(id);
        if (!success) {
            ErrorCodeEnum.RED_SET_BLOOM_FAIl.throwException();
        }
    }

    /**
     * 聊天校验器
     */
    private class ChatVerifier implements RedEnvelopeVerifier {
        @Override
        public void verifier(Long uid, RedEnvelope redEnvelope) {
            RedEnvelopeChannel channel = redEnvelope.getChannel();

            if (Objects.nonNull(channel) && !RedEnvelopeChannel.CHAT.equals(channel)) {
                return;
            }

            if (StringUtils.isBlank(redEnvelope.getFlag())) {
                throw ErrorCodeEnum.RED_FLAG_NULL.generalException();
            }

            BigDecimal totalAmount = redEnvelope.getTotalAmount();
            String coin = redEnvelope.getCoin();
            BigDecimal uAmount = currencyService.getDollarRate(coin).multiply(totalAmount);
            if (uAmount.compareTo(BigDecimal.valueOf(100L)) > 0) {
                ErrorCodeEnum.RED_AMOUNT_EXCEED_LIMIT_100.throwException();
            }

            AccountBalance accountBalance = accountBalanceService.getAndInit(uid, coin);
            if (totalAmount.compareTo(accountBalance.getRemain()) > 0) {
                ErrorCodeEnum.INSUFFICIENT_BALANCE.throwException();
            }
        }
    }

    /**
     * 邀请红包校验器
     */
    private class InviteVerifier implements RedEnvelopeVerifier {
        @Override
        public void verifier(Long uid, RedEnvelope redEnvelope) {
            if (!RedEnvelopeChannel.EXTERN.equals(redEnvelope.getChannel())) {
                return;
            }
            RedEnvelopeConfig redEnvelopeConfig = redEnvelopeConfigService.getOne(redEnvelope.getCoin(), redEnvelope.getChannel());
            redEnvelopeConfig = MoreObjects.firstNonNull(redEnvelopeConfig, RedEnvelopeConfig.externDefaultConfig());

            BigDecimal totalAmount = redEnvelope.getTotalAmount();
            int num = redEnvelope.getNum();

            // 数量为0 或者 大于配置金额
            if (num == 0 || num > redEnvelopeConfig.getNum()) {
                ErrorCodeEnum.RED_NUM_ERROR.throwException();
            }

            // 金额为0或者大于配置金额
            var totalMinAmount =
                    redEnvelopeConfig.getMinAmount().multiply(BigDecimal.valueOf(redEnvelope.getNum()));
            if (totalAmount.compareTo(redEnvelopeConfig.getLimitAmount()) > 0
                    || totalAmount.compareTo(BigDecimal.ZERO) == 0
                    || totalAmount.compareTo(totalMinAmount) < 0) {
                throw new CustomException(ErrorCodeEnum.RED_TOTAL_AMOUNT_LIMIT.getErrorNo(), redEnvelope.getCoin().toUpperCase() + ErrorCodeEnum.RED_TOTAL_AMOUNT_LIMIT.getErrorMsg() + redEnvelopeConfig.getLimitAmount()
                        .stripTrailingZeros().toPlainString() + redEnvelope.getCoin().toUpperCase());
            }

            // 获取配置项目小数点位数
            int scale = redEnvelopeConfig.getScale();

            var averageValueSeventyPercent =
                    RedEnvelopeGiveStrategy.getAverageValue70Percent(redEnvelope.getTotalAmount(), redEnvelope.getNum(), scale);
            if (averageValueSeventyPercent.compareTo(redEnvelopeConfig.getMinAmount()) < 0) {
                ErrorCodeEnum.throwException("当前红包平均值70近似值%小于最小金额，近似平均值："
                        + averageValueSeventyPercent.toPlainString() + "   设定最小值："
                        + redEnvelopeConfig.getMinAmount().toPlainString());
            }

            if (averageValueSeventyPercent.multiply(new BigDecimal(redEnvelope.getNum() + "")).compareTo(redEnvelope.getTotalAmount()) > 0) {
                ErrorCodeEnum.throwException("当前红包平均值70近似值乘红包数量大于红包总价，近似平均值："
                        + averageValueSeventyPercent.toPlainString() + "   红包总金额："
                        + redEnvelope.getTotalAmount().toPlainString());
            }
        }
    }

    private String getExternUrl(RedEnvelopeChannel channel, Long id) {
        if (!RedEnvelopeChannel.EXTERN.equals(channel)) {
            return null;
        }

        ConfigService bean = ApplicationContextTool.getBean(ConfigService.class);
        bean = Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
        return bean
                .getOrDefault(SYSTEM_URL_PATH_PREFIX, "https://www.assureadd.com")
                + "/packet/openapi/red/redPackage?context="
                + PBE.encryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, id + "").replace("+", "%2B");
    }


    public static void main(String[] args) {

        String s = PBE.encryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, "1757699208289090917");
        log.info(s);
    }


}
