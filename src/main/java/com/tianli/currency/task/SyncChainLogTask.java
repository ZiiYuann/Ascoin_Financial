package com.tianli.currency.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.chain.mapper.ChainLog;
import com.tianli.chain.mapper.ChainLogMapper;
import com.tianli.chain.service.ChainLogService;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private ChainLogService chainLogService;

    @Resource
    private RedisLock redisLock;

    private static final String SYNC_CHAIN_LOG_TASK_KEY = "Sync:ChainLog:Task";

    /**
     * 每日计算利息task
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void syncChainLog() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock(SYNC_CHAIN_LOG_TASK_KEY, 15L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }


            long maxLogId = chainLogMapper.maxId();
            List<Charge> chargeList = chargeService.list(Wrappers.lambdaQuery(Charge.class)
                    .eq(Charge::getStatus, ChargeStatus.chain_success)
                    .eq(Charge::getCharge_type, ChargeType.recharge)
                    .gt(Charge::getId, maxLogId)
                    .last("LIMIT 50")
            );
            if (CollectionUtils.isEmpty(chargeList)) {
                return;
            }
            List<ChainLog> chainLogList = chargeList.stream().map(e -> {
                ChainLog chainLog = new ChainLog();
                chainLog.setId(e.getId());
                chainLog.setAddress(e.getTo_address());
                chainLog.setCurrency_type(e.getCurrency_type());
                chainLog.setAmount(e.getAmount());
                chainLog.setUid(e.getUid());
                chainLog.setUsername(e.getUid_username());
                chainLog.setU_create_time(e.getCreate_time());
                return chainLog;
            }).collect(Collectors.toList());
            try {
                chainLogService.replaceBatch(chainLogList);
            } catch (Exception e) {
                log.error("syncChainLog ERROR, ", e);
            }
        });
    }
}
