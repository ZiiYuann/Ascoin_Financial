package com.tianli.chain.service;

import com.tianli.chain.service.contract.BtcOperation;
import com.tianli.common.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author cs
 * @Date 2023-01-05 11:03
 */
//@Component
public class BtcMainAddressGenerateRunnerTmp implements ApplicationRunner {
    public static final String BTC_MAIN_ADDRESS_GENERATE_LOCK = "btc:main:address:generate:lock";
    @Resource
    private ConfigService configService;
    @Resource
    private RedissonClient redisson;
    @Resource
    private BtcOperation btcOperation;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String addr = configService._get(ConfigConstants.BTC_MAIN_WALLET_ADDRESS);
        if(addr == null) {
            RLock lock = redisson.getLock(BTC_MAIN_ADDRESS_GENERATE_LOCK);
            if (lock.tryLock()) {
                try {
                    String mnemonic = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
                    String address = btcOperation.generateAddress(mnemonic);
                    configService.replace(ConfigConstants.BTC_MAIN_WALLET_ADDRESS, address);
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        }
    }
}
