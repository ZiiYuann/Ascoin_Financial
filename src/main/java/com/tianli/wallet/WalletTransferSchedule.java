package com.tianli.wallet;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.GraphService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.wallet.mapper.MainWalletLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.mountcloud.graphql.GraphqlClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

//@Service
@Slf4j
//@RestController
public class WalletTransferSchedule {

    @Scheduled(fixedDelay = 500)
//    @GetMapping("/tast/mainWalletBscTransfer")
    public void mainWalletBscTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletBscTransfer", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                walletTransferService.mainBscWalletScan(ApplicationContextTool.getBean("graphqlClient", GraphqlClient.class));
            } catch (Exception e) {
                log.error("mainWalletBscTransfer Exception:", e);
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletBscTransfer");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
//    @GetMapping("/tast/mainWalletEthTransfer")
    public void mainWalletEthTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletEthTransfer", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                walletTransferService.mainEthWalletScan(ApplicationContextTool.getBean("ethGraphqlClient", GraphqlClient.class));
            } catch (Exception e) {
                log.error("mainWalletEthTransfer Exception:", e);
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletEthTransfer");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
//    @GetMapping("/tast/mainWalletTrcTransfer")
    public void mainWalletTrcTransfer() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("WalletTransferService:mainWalletTrcTransfer", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                String value = configService._get(ConfigConstants.TRON_MAIN_WALLET_TRANSFER_BLOCK);
                Long nowBlock = value == null ? null : Long.valueOf(value);
                Protocol.Block block = blockingStub.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
                long blockcount = block.getBlockHeader().getRawData().getNumber();
                if (nowBlock == null)
                    nowBlock = blockcount;
                for (long i = nowBlock + 1; i <= blockcount; i++) {
                    walletTransferService.mainTrcWalletScan(i);
                }
            } catch (Exception e) {
                log.error("mainWalletTrcTransfer Exception:", e);
            } finally {
                redisLock.unlock("WalletTransferService:mainWalletTrcTransfer");
            }
        });
    }

    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ConfigService configService;
    @Resource
    private GraphService graphService;
    @Resource
    private MainWalletLogMapper mainWalletLogMapper;
    @Resource
    private WalletTransferService walletTransferService;
    @Resource
    private WalletGrpc.WalletBlockingStub blockingStub;
}
