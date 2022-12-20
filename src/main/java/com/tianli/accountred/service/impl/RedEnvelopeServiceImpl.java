package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.accountred.convert.RedEnvelopeConvert;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.accountred.enums.RedEnvelopeWay;
import com.tianli.accountred.mapper.RedEnvelopeMapper;
import com.tianli.accountred.query.RedEnvelopeChainQuery;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.query.RedEnvelopeIoUQuery;
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
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisService;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.tool.ApplicationContextTool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
// TODO 1、抢红包任何阻塞性操作都改为sqs异步消费 2、如果缓存丢失，如何补偿缓存
@Slf4j
@Service
public class RedEnvelopeServiceImpl extends ServiceImpl<RedEnvelopeMapper, RedEnvelope> implements RedEnvelopeService {

    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private RedEnvelopeConvert redEnvelopeConvert;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;
    @Resource
    private RedEnvelopeSpiltGetRecordService redEnvelopeSpiltGetRecordService;
    @Resource
    private OrderService orderService;
    @Resource
    private RedisService redisService;
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

    @PostConstruct
    public void initBloomFilter() {
        // 布隆过滤器，防止缓存击穿
        final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + "bloom");

        if (bloomFilter.isExists()) {
            return;
        }

        log.info("初始化布隆过滤器【red:bloom】");
        bloomFilter.tryInit(10000000L, 0.001f);
        final List<Long> ids = redEnvelopeMapper.listIds();
        ids.forEach(bloomFilter::add);

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

        setRedisCache(redEnvelope);
        setBloomCache(redEnvelope.getId());
        return Result.success(new RedEnvelopeGiveVO(redEnvelope.getId()));
    }

    /**
     * 具体拆分红包
     *
     * @param uid         uid
     * @param redEnvelope 红包
     */
    private void spiltRedEnvelope(Long uid, RedEnvelope redEnvelope) {

        validGiveRedEnvelope(uid, redEnvelope.getTotalAmount(), redEnvelope.getCoin());

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
        accountBalanceServiceImpl.decrease(uid, ChargeType.red_give, redEnvelope.getCoin(), redEnvelope.getTotalAmount()
                , order.getOrderNo(), "发红包");
    }

    @Override
    @SneakyThrows
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Result give(Long uid, Long shortUid, RedEnvelopeChainQuery query) {
        RedEnvelope redEnvelope = this.getWithCache(query.getId());
        Optional.ofNullable(redEnvelope).orElseThrow(ErrorCodeEnum.RED_NOT_EXIST::generalException);

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
        return Result.success(new RedEnvelopeGiveVO(redEnvelope.getId()));
    }

    @Override
    @SneakyThrows
    public RedEnvelopeGetVO get(Long uid, Long shortUid, RedEnvelopeGetQuery query) {
        // 判断红包缓存是否存在
        RedEnvelope redEnvelope = this.getWithCache(query.getRid());
        Optional.ofNullable(redEnvelope).orElseThrow(ErrorCodeEnum.RED_NOT_EXIST::generalException);
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
        if (RedEnvelopeStatus.WAIT.equals(redEnvelope.getStatus())
                || RedEnvelopeStatus.FAIL.equals(redEnvelope.getStatus())
                || RedEnvelopeStatus.OVERDUE.equals(redEnvelope.getStatus())
                || RedEnvelopeStatus.FINISH.equals(redEnvelope.getStatus())) {
            return new RedEnvelopeGetVO(redEnvelope.getStatus(), coinBase);
        }

        RedEnvelopeServiceImpl service = ApplicationContextTool.getBean(RedEnvelopeServiceImpl.class);
        Optional.ofNullable(service).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);

        query.setRedEnvelope(redEnvelope);

        // 单独的事务，此事务已经提交
        RedEnvelopeGetVO operation = service.getOperation(uid, shortUid, query, redEnvelope);

        // 缓存双删
        Thread.sleep(100);
        this.deleteRedisCache(query.getRid());
        return operation;
    }

    @Override
    public RedEnvelopeGetDetailsVO getDetails(Long uid, Long rid) {
        RedEnvelope redEnvelope = getWithCache(rid);
        List<RedEnvelopeSpiltGetRecordVO> recordVo = redEnvelopeSpiltGetRecordService.getRecordVos(rid);

        RedEnvelopeGetDetailsVO redEnvelopeGetDetailsVO = redEnvelopeConvert.toRedEnvelopeGetDetailsVO(redEnvelope);
        redEnvelopeGetDetailsVO.setRecords(recordVo);

        RedEnvelopeSpiltGetRecord record = redEnvelopeSpiltGetRecordService.getRecord(rid, uid);
        redEnvelopeGetDetailsVO.setReceiveAmount(Objects.isNull(record) ? null : record.getAmount());
        redEnvelopeGetDetailsVO.setUReceiveAmount(Objects.isNull(record) ? null
                : record.getAmount().multiply(currencyService.getDollarRate(redEnvelope.getCoin())));

        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        redEnvelopeGetDetailsVO.setCoinUrl(coinBase.getLogo());
        return redEnvelopeGetDetailsVO;
    }

    @Transactional
    public RedEnvelopeGetVO getOperation(Long uid, Long shortUid, RedEnvelopeGetQuery query, RedEnvelope redEnvelope) {

        String receivedKey = RedisConstants.SPILT_RED_ENVELOPE_GET + query.getRid() + ":" + uid;
        String spiltRedKey = RedisConstants.SPILT_RED_ENVELOPE + query.getRid();
        // 此lua脚本的用处 1、判断用户是否已经抢过红包 2、判断拆分红包是否还有剩余
        String script =
                "if redis.call('EXISTS', KEYS[1]) > 0 then\n" +
                        "    return 'RECEIVED'\n" +
                        "elseif redis.call('EXISTS', KEYS[2]) == 0 then\n" +
                        "    return 'FINISH'\n" +
                        "else\n" +
                        "    redis.call('SET',KEYS[1],'')\n" +
                        "    redis.call('EXPIRE',KEYS[1],86400)\n" +
                        "    return redis.call('SPOP', KEYS[2])\n" +
                        "end";
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptText(script);
        String result;
        try {
            result = stringRedisTemplate.opsForValue().getOperations().execute(redisScript, List.of(receivedKey, spiltRedKey));
        } catch (Exception e) {
            // 防止由于网络波动导致的红包异常 暂时做通知处理，如果情况比较多，后续进行补偿
            webHookService.dingTalkSend("领取红包状态异常，请及时处理，红包id：" + query.getRid() + " uid：" + uid, e);
            throw e;
        }

        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        if (RedEnvelopeStatus.FINISH.name().equals(result)) {
            return new RedEnvelopeGetVO(RedEnvelopeStatus.FINISH, coinBase);
        }
        if (RedEnvelopeStatus.RECEIVED.name().equals(result)) {
            return new RedEnvelopeGetVO(RedEnvelopeStatus.RECEIVED, coinBase);
        }

        // 领取子红包（创建订单，余额操作）
        RedEnvelopeSpilt redEnvelopeSpilt = redEnvelopeSpiltService.getRedEnvelopeSpilt(uid, shortUid, result, query);
        // 增加已经领取红包个数
        int i = this.getBaseMapper().increaseReceiveNum(query.getRid());
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }

        redEnvelope = this.getById(query.getRid());
        if (redEnvelope.getNum() == redEnvelope.getReceiveNum()) {
            this.finish(query.getRid());
        }

        RedEnvelopeGetVO redEnvelopeGetVO = new RedEnvelopeGetVO(RedEnvelopeStatus.SUCCESS, coinBase);
        redEnvelopeGetVO.setReceiveAmount(redEnvelopeSpilt.getAmount());
        redEnvelopeGetVO.setUReceiveAmount(currencyService.getDollarRate(redEnvelope.getCoin()).multiply(redEnvelopeSpilt.getAmount()));
        redEnvelopeGetVO.setUid(redEnvelope.getUid());
        redEnvelopeGetVO.setShortUid(redEnvelope.getShortUid());

        // 删除红包以及领取记录以及当前红包领取记录的缓存
        this.deleteRedisCache(query.getRid());
        redisTemplate.delete(RedisConstants.RED_ENVELOPE_GET_RECORD + query.getRid());
        return redEnvelopeGetVO;
    }

    @Override
    public IPage<RedEnvelopeGiveRecordVO> giveRecord(Long uid, PageQuery<RedEnvelope> pageQuery) {
        Page<RedEnvelope> page = this.page(pageQuery.page()
                , new LambdaQueryWrapper<RedEnvelope>().eq(RedEnvelope::getUid, uid)
                        .ne(RedEnvelope::getStatus, RedEnvelopeStatus.WAIT)
                        .last(" order by create_time desc "));
        return page.convert(redEnvelopeConvert::toRedEnvelopeGiveRecordVO);
    }

    @Override
    @Transactional
    public void redEnvelopeExpiration(LocalDateTime now) {
        // 2022-10-10 13:00:00 创建 如果当前时间是 2022-10-11 14:00:00（-1  2022-10-10 14:00:00）
        // plusMinutes(1) 等待缓存过期，确保过期操作内不会有人拿红包
        LocalDateTime dateTime = now.plusDays(-1).plusMinutes(1);
        LambdaQueryWrapper<RedEnvelope> queryWrapper = new LambdaQueryWrapper<RedEnvelope>()
                .eq(RedEnvelope::getStatus, RedEnvelopeStatus.PROCESS)
                .lt(RedEnvelope::getCreateTime, dateTime);

        List<RedEnvelope> redEnvelopes = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(redEnvelopes)) {
            redEnvelopes.forEach(redEnvelope -> {
                try {
                    RedEnvelopeServiceImpl bean = ApplicationContextTool.getBean(RedEnvelopeServiceImpl.class);
                    if (Objects.isNull(bean)) {
                        ErrorCodeEnum.SYSTEM_ERROR.throwException();
                    }
                    bean.redEnvelopeRollback(redEnvelope);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void redEnvelopeRollback(RedEnvelope redEnvelope) {
        List<RedEnvelopeSpilt> spiltRedEnvelopes =
                redEnvelopeSpiltService.getRedEnvelopeSpilt(redEnvelope.getId(), false);

        // 进入这个方法的红包应该存在红包未领取
        if (CollectionUtils.isEmpty(spiltRedEnvelopes)) {
            webHookService.dingTalkSend("红包状态不为领取完，但是拆分红包不存在，请排查异常：" + redEnvelope.getId());
            return;
        }

        BigDecimal rollbackAmount = spiltRedEnvelopes.stream().map(RedEnvelopeSpilt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 红包回滚订单
        Order order = Order.builder()
                .uid(redEnvelope.getUid())
                .coin(redEnvelope.getCoin())
                .orderNo(AccountChangeType.red_back.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(rollbackAmount)
                .type(ChargeType.red_back)
                .completeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .status(ChargeStatus.chain_success)
                .relatedId(redEnvelope.getId())
                .build();
        orderService.save(order);

        accountBalanceServiceImpl.increase(redEnvelope.getUid(), ChargeType.red_back, redEnvelope.getCoin(), rollbackAmount
                , order.getOrderNo(), "红包到期回退");

        this.overdue(redEnvelope.getId());

    }


    /**
     * 获取红包信息（缓存）
     * 1、发红包（上链）
     * 2、抢红包
     * 3、红包详情
     * 4、单独调用查询红包状态
     */
    private RedEnvelope getWithCache(Long id) {
        // 布隆过滤器，防止缓存击穿
        final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + "bloom");
        if (!bloomFilter.contains(id)) {
            ErrorCodeEnum.RED_NOT_EXIST_BLOOM.throwException();
        }

        Object cache = redisService.get(RedisConstants.RED_ENVELOPE + id);
        if (Objects.isNull(cache)) {
            RedEnvelope redEnvelope = this.getById(id);
            if (Objects.isNull(redEnvelope)) {
                ErrorCodeEnum.RED_NOT_EXIST.throwException();
            }
            setRedisCache(redEnvelope);
            return redEnvelope;
        }
        return (RedEnvelope) cache;
    }

    /**
     * 把红包状态设置过期
     */
    private void overdue(Long id) {
        int i = this.getBaseMapper().overdue(id);
        if (i == 0) {
            ErrorCodeEnum.RED_STATUS_ERROR.throwException();
        }
    }


    /**
     * 把红包状态设置为结束
     */
    private void finish(Long id) {
        int i = this.getBaseMapper().finish(id);
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
    private void deleteRedisCache(Long id) {
        redisTemplate.delete(RedisConstants.RED_ENVELOPE + id);
    }

    /**
     * 增加红包缓存
     * 1、发红包本地
     * 2、发红包链上
     * 3、getWithCache
     */
    private void setRedisCache(RedEnvelope redEnvelope) {
        redisService.set(RedisConstants.RED_ENVELOPE + redEnvelope.getId(), redEnvelope, 1L, TimeUnit.DAYS);
    }

    /**
     * 设置步隆过滤器缓存
     */
    private void setBloomCache(Long id) {
        final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + "bloom");
        boolean success = bloomFilter.add(id);
        if (!success) {
            ErrorCodeEnum.RED_SET_BLOOM_FAIl.throwException();
        }
    }

    /**
     * 校验发红包
     */
    private void validGiveRedEnvelope(Long uid, BigDecimal totalAmount, String coin) {

        BigDecimal uAmount = currencyService.getDollarRate(coin).multiply(totalAmount);
        if (uAmount.compareTo(BigDecimal.valueOf(100L)) > 0) {
            ErrorCodeEnum.RED_AMOUNT_EXCEED_LIMIT_100.throwException();
        }

        AccountBalance accountBalance = accountBalanceServiceImpl.getAndInit(uid, coin);
        if (totalAmount.compareTo(accountBalance.getRemain()) > 0) {
            ErrorCodeEnum.INSUFFICIENT_BALANCE.throwException();
        }

    }

}
