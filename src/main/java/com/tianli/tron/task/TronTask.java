package com.tianli.tron.task;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tron.service.TronService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author cs
 * @Date 2022-01-07 2:45 下午
 */
@Component
public class TronTask {

    @Resource
    private WalletGrpc.WalletBlockingStub blockingStub;
    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ConfigService configService;
    @Resource
    private TronService tronService;

    /**
     * tron用户充值监控任务
     */
//    @Scheduled(cron = "0/3 * * * * ?")
//    public void task(){
//        asyncService.async(() -> {
//            redisLock.lock("trxTxScheduled.tx", 5L, TimeUnit.MINUTES);
//            String value = configService._get(TronService.TRX_BLOCK_COUNT);
//            Long nowBlock = value == null ? null : Long.valueOf(value);
//            try {
//                Protocol.Block block = blockingStub.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
//                long blockcount = block.getBlockHeader().getRawData().getNumber();
//                if (nowBlock == null)
//                    nowBlock = blockcount;
//                for (long i = nowBlock + 1; i <= blockcount; i++) {
//                    tronService.auto(i);
//                }
//            }catch (Exception e){
//                ErrorCodeEnum.NETWORK_ERROR.throwException();
//            }
//        });
//    }
}
