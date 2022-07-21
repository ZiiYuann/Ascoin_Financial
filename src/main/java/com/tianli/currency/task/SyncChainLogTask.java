package com.tianli.currency.task;

import com.tianli.chain.mapper.ChainLogMapper;
import com.tianli.chain.service.WalletImputationService;
import com.tianli.charge.service.ChargeService;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SyncChainLogTask {

    @Resource
    private AsyncService asyncService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private ChainLogMapper chainLogMapper;

    @Resource
    private WalletImputationService chainLogService;

    @Resource
    private RedisLock redisLock;

    private static final String SYNC_CHAIN_LOG_TASK_KEY = "Sync:ChainLog:Task";


}
