package com.tianli.currency_token.transfer.schedule;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.transfer.TokenTransferService;
import com.tianli.currency_token.transfer.TransferRequestService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
/**
 * 充值提现
 */
@Component
@Slf4j
public class TokenTransferSchedule {

    @Scheduled(fixedDelay = 500)
    public void bscTokenTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("TokenTransferSchedule:tokenTransfer:bsc", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                long block = Long.parseLong(configService.get(ConfigConstants.SYNC_TRANSFER_BSC_BLOCK));
                long latestBlock = transferRequestService.latestBlock(ChainType.bep20);
                if(block + 1 <= latestBlock) tokenTransferService.syncBscTransfer(block + 1);
            } catch (Exception e) {
                log.error("tokenTransfer Exception:", e);
            } finally {
                redisLock.unlock("TokenTransferSchedule:tokenTransfer:bsc");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void ercTokenTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("TokenTransferSchedule:tokenTransfer:erc", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                long block = Long.parseLong(configService.get(ConfigConstants.SYNC_TRANSFER_ERC_BLOCK));
                long latestBlock = transferRequestService.latestBlock(ChainType.erc20);
                if(block + 1 <= latestBlock) tokenTransferService.syncErcTransfer(block + 1);
            } catch (Exception e) {
                log.error("tokenTransfer Exception:", e);
            } finally {
                redisLock.unlock("TokenTransferSchedule:tokenTransfer:erc");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void trcTokenTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("TokenTransferSchedule:tokenTransfer:trc", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                long block = Long.parseLong(configService.get(ConfigConstants.SYNC_TRANSFER_TRC_BLOCK));
                long latestBlock = transferRequestService.latestBlock(ChainType.trc20);
                if(block + 1 <= latestBlock) tokenTransferService.syncTrcTransfer(block + 1);
            } catch (Exception e) {
                log.error("tokenTransfer Exception:", e);
            } finally {
                redisLock.unlock("TokenTransferSchedule:tokenTransfer:trc");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void sgRecharge() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("TokenTransferSchedule:sgRecharge", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                tokenTransferService.syncRechargeWithdraw();
            } catch (Exception e) {
                log.error("tokenTransfer Exception:", e);
            } finally {
                redisLock.unlock("TokenTransferSchedule:sgRecharge:trc");
            }
        });
    }


    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private TokenTransferService tokenTransferService;
    @Resource
    private ConfigService configService;
    @Resource
    private TransferRequestService transferRequestService;
}
