package com.tianli.accountred.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.base.MoreObjects;
import com.tianli.account.query.IdsQuery;
import com.tianli.accountred.dto.RedEnvelopeSpiltDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.query.*;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.*;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.annotation.AppUse;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.init.RequestInitService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@RestController
@RequestMapping("/red")
public class RedEnvelopeController {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedEnvelopeSpiltGetRecordService redEnvelopeSpiltGetRecordService;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private CurrencyService currencyService;

    @Resource
    private RedEnvelopeConfigService redEnvelopeConfigService;

    /**
     * 发红包
     */
    @AppUse
    @PostMapping("/give")
    public Result<RedEnvelopeGiveVO> give(@RequestBody @Valid RedEnvelopeIoUQuery query) {
        Long uid = requestInitService.uid();

        Long shortUid = requestInitService.get().getUserInfo().getChatId();
        if (Objects.isNull(shortUid)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }
        RLock lock = redissonClient.getLock(RedisLockConstants.RED_ENVELOPE_GIVE + uid);
        try {
            lock.lock();
            return redEnvelopeService.give(uid, shortUid, query);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 发红包记录
     */
    @AppUse
    @GetMapping("/give/record")
    public Result<IPage<RedEnvelopeGiveRecordVO>> giveRecord(PageQuery<RedEnvelope> pageQuery, RedEnvelopeChannel channel) {
        RedEnvelopeGiveRecordQuery query = RedEnvelopeGiveRecordQuery.builder()
                .uid(requestInitService.uid())
                .channel(MoreObjects.firstNonNull(channel, RedEnvelopeChannel.CHAT))
                .build();
        return new Result<>(redEnvelopeService.giveRecord(query, pageQuery));
    }


    /**
     * 发红包信息
     */
    @AppUse
    @GetMapping("/get/{id}")
    public Result<RedEnvelopeGetVO> getInfoById(@PathVariable("id") Long id) {
        return new Result<>(redEnvelopeService.getInfoById(id));
    }

    /**
     * 发红包
     */
    @AppUse
    @PostMapping("/give/txid")
    public Result<RedEnvelopeGiveVO> giveTxid(@RequestBody @Valid RedEnvelopeChainQuery query) {
        Long uid = requestInitService.uid();
        RLock lock = redissonClient.getLock(RedisLockConstants.RED_ENVELOPE_GIVE + uid);
        try {
            lock.lock();
            return redEnvelopeService.give(uid, requestInitService.get().getUserInfo().getChatId(), query);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 抢红包
     */
    @AppUse
    @PostMapping("/get")
    public Result<RedEnvelopeGetVO> get(@RequestBody @Valid RedEnvelopeGetQuery query) {
        Long uid = requestInitService.uid();
        Long shortUid = requestInitService.get().getUserInfo().getChatId();
        if (Objects.isNull(shortUid)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }
        return new Result<>(redEnvelopeService.get(uid, shortUid, query));
    }

    /**
     * 兑换红包
     */
    @AppUse
    @PostMapping("/exchange")
    public Result<RedEnvelopeGetVO> exchange(@RequestBody @Valid RedEnvelopeExchangeCodeQuery query) {
        Long uid = requestInitService.uid();
        Long shortUid = requestInitService.get().getUserInfo().getChatId();
        if (Objects.isNull(shortUid)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }
        return new Result<>(redEnvelopeService.get(uid, shortUid, query));
    }

    /**
     * 抢红包详情
     */
    @AppUse
    @GetMapping("/get/details/{id}")
    public Result<RedEnvelopeGetDetailsVO> get(@PathVariable Long id) {
        Long uid = requestInitService.uid();
        return new Result<>(redEnvelopeService.getDetails(uid, id));
    }

    /**
     * 领取红包记录
     */
    @AppUse
    @GetMapping("/get/record")
    public Result<IPage<RedEnvelopeSpiltGetRecordVO>> getRecord(PageQuery<RedEnvelopeSpiltGetRecord> pageQuery
            , RedEnvelopeChannel channel) {
        Long uid = requestInitService.uid();
        channel = MoreObjects.firstNonNull(channel, RedEnvelopeChannel.CHAT);
        return new Result<>(redEnvelopeSpiltGetRecordService.getRecords(uid, channel, pageQuery));
    }

    /**
     * 领取红包记录
     */
    @AppUse
    @PostMapping("/back")
    public Result<Void> back(@RequestBody IdsQuery idsQuery) {
        Long rid = idsQuery.getId();
        Long uid = requestInitService.uid();
        redEnvelopeService.backRed(uid, rid);
        return new Result<>();
    }

    @AppUse
    @GetMapping("/exchange/{exchangeCode}")
    public Result<RedEnvelopeExchangeCodeVO1> redInfo(@PathVariable String exchangeCode) {
        RedEnvelopeSpiltDTO redEnvelopeSpiltDTO =
                redEnvelopeSpiltService.getRedEnvelopeSpiltDTOCache(exchangeCode);
        String rid = redEnvelopeSpiltDTO.getRid();
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.valueOf(rid));
        CoinBase coinBase = coinBaseService.getByName(redEnvelope.getCoin());
        RedEnvelopeExchangeCodeVO1 result  = RedEnvelopeExchangeCodeVO1.builder()
                .coin(redEnvelope.getCoin())
                .coinUrl(coinBase.getLogo())
                .receiveAmount(redEnvelopeSpiltDTO.getAmount())
                .usdtRate(currencyService.getDollarRate(redEnvelope.getCoin()))
                .flag(redEnvelope.getFlag()).build();
        return new Result<>(result);
    }

    /**
     * 红包配置列表
     */
    @AppUse
    @GetMapping("/configs")
    public Result<List<RedEnvelopeConfigVO>> configs(@RequestParam("channel") String channel
            , @RequestParam(value = "coin", required = false) String coin) {
        return new Result<>(redEnvelopeConfigService.getList(channel, coin));
    }
}
