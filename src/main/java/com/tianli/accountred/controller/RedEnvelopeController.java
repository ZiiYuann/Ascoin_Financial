package com.tianli.accountred.controller;

import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.query.RedEnvelopeChainQuery;
import com.tianli.accountred.query.RedEnvelopeGetQuery;
import com.tianli.accountred.query.RedEnvelopeIoUQuery;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltGetRecordService;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.init.RequestInitService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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

    /**
     * 发红包
     */
    @PostMapping("/give")
    public Result give(@RequestBody @Valid RedEnvelopeIoUQuery query) {
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
    @GetMapping("/give/record")
    public Result giveRecord(PageQuery<RedEnvelope> pageQuery) {
        Long uid = requestInitService.uid();
        return Result.success().setData(redEnvelopeService.giveRecord(uid, pageQuery));
    }


    /**
     * 发红包信息
     */
    @GetMapping("/get/{id}")
    public Result getInfoById(@PathVariable("id") Long id) {
        return Result.success().setData(redEnvelopeService.getInfoById(id));
    }

    /**
     * 发红包
     */
    @PostMapping("/give/txid")
    public Result giveTxid(@RequestBody @Valid RedEnvelopeChainQuery query) {
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
    @PostMapping("/get")
    public Result get(@RequestBody @Valid RedEnvelopeGetQuery query) {
        Long uid = requestInitService.uid();
        Long shortUid = requestInitService.get().getUserInfo().getChatId();
        if (Objects.isNull(shortUid)) {
            ErrorCodeEnum.ACCOUNT_ERROR.throwException();
        }
        return Result.success().setData(redEnvelopeService.get(uid, shortUid, query));
    }

    /**
     * 抢红包详情
     */
    @GetMapping("/get/details/{id}")
    public Result get(@PathVariable Long id) {
        Long uid = requestInitService.uid();
        return Result.success(redEnvelopeService.getDetails(uid, id));
    }

    /**
     * 领取红包记录
     */
    @GetMapping("/get/record")
    public Result getRecord(PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {
        Long uid = requestInitService.uid();
        return Result.success().setData(redEnvelopeSpiltGetRecordService.getRecord(uid, pageQuery));
    }

}