package com.tianli.chain.service.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.FinancialApplication;
import com.tianli.common.ConfigConstants;
import com.tianli.common.RedisConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.task.FinancialIncomeTask;
import com.tianli.task.FundIncomeTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FinancialApplication.class)
@Slf4j
class BootTest {

    @Resource
    private FundIncomeTask fundIncomeTask;

    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private FinancialIncomeTask financialIncomeTask;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private BscTriggerContract bscTriggerContract;
    @Resource
    private ConfigService configService;

    @Test
    void estimateGasTron() {
        String contractAddress = configService.get(ConfigConstants.TRON_TRIGGER_ADDRESS);
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        tronTriggerContract.recycle(ownerAddress, List.of(1742187300464117328L), List.of("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"));
    }

    @Test
    void estimateGasEth() {
        String fromAddress = bscTriggerContract.getMainWalletAddress();
        String data = bscTriggerContract.buildRecycleData(fromAddress, List.of(1740215892861153609L, 1747030106832694150L)
                , List.of("0x55d398326f99059ff775485246999027b3197955"));
        String gas = bscTriggerContract.getGas();
        log.info("gas:" + gas);
        BigInteger bigInteger = bscTriggerContract.estimateGas(fromAddress, "0xd50d290afcfc83d88e0cd31ddd12688ffa16cc07","0"
                , null, null, data);
        log.info(bigInteger.toString());
    }

    @Test
    void income() throws InterruptedException {
        while (true) {
            List<FinancialRecord> records = financialRecordService.list(

                    new LambdaQueryWrapper<FinancialRecord>()
                            .in(FinancialRecord::getId, List.of(1744213468616162991L))
            );

            records.forEach(record -> financialIncomeTask.incomeExternalTranscation(record));
            Thread.sleep(5000);

        }
    }

    @Test
    void fundIncome() throws InterruptedException {
        while (true) {
            FundRecord record = fundRecordService.getById(
                    1571691088468738050L
            );

            fundIncomeTask.calculateIncome(record, LocalDateTime.now());
            Thread.sleep(5000);

        }
    }

    @Test
    void contract() throws InterruptedException, IOException {
        while (true) {
            final RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstants.RED_ENVELOPE + "bloom");
            Boolean add = bloomFilter.add(1752627082913970530L);
            log.info("123" + add);
        }

    }


}