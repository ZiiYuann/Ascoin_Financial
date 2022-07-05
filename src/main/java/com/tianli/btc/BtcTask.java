package com.tianli.btc;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.mconfig.ConfigService;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author cs
 * @Date 2022-01-18 9:43 上午
 */
//@Component
public class BtcTask {
    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ConfigService configService;
    @Resource
    private RpcService rpcService;
    @Resource
    private BtcService btcService;

//    @Scheduled(cron = "19 0/2 * * * ?")
    public void tx() {
        asyncService.async(() -> {
            redisLock.lock("TxScheduled.tx", 20L, TimeUnit.MINUTES);
            String config = configService._get(BtcService.BLOCK_COUNT);
            Long nowBlock = config == null ? null : Long.valueOf(config);
            long blockcount = rpcService.getblockcount();
            if (nowBlock == null)
                nowBlock = blockcount;
            for (long i = nowBlock + 1; i <= blockcount; i++) {
                redisLock._addExpireTime("TxScheduled.tx", 15L, TimeUnit.MINUTES, 300L);
                btcService.auto(i);
            }
        });
    }
}
