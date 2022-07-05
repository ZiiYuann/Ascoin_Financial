package com.tianli.wallet;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @date 2022/4/26 13:55
 */
@Component
@Slf4j
public class WalletTransferScheduleV2 {

    @Scheduled(fixedDelay = 500)
    public void mainWalletBscTransferV2() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletBscTransferV2", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                walletTransferServiceV2.mainBscWalletScan();
            } catch (Exception e) {
                log.error("mainWalletBscTransferV2 Exception:{}", e.toString());
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletBscTransferV2");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void mainWalletEthTransferV2() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletEthTransferV2", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                walletTransferServiceV2.mainEthWalletScan();
            } catch (Exception e) {
                log.error("mainWalletEthTransferV2 Exception:{}", e.toString());
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletEthTransferV2");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void mainWalletTrcTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletTrcTransferV2", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                walletTransferServiceV2.mainTrcWalletScan();
            } catch (Exception e) {
                log.error("mainWalletTrcTransferV2 Exception:{}", e.toString());
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletTrcTransferV2");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void mainWalletCollection() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletCollection", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                walletTransferServiceV2.mainWalletCollection();
            } catch (Exception e) {
                log.error("mainWalletCollection Exception:{}", e.toString());
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletCollection");
            }
        });
    }



    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    WalletTransferServiceV2 walletTransferServiceV2;
}
