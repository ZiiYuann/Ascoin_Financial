package com.tianli.chain.service.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.FinancialApplication;
import com.tianli.common.RedisConstants;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.LiquidateDTO;
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
import java.math.BigDecimal;
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
    private RpcService rpcService;

    @Test
    void rpcTest() {
        String bnb = rpcService.liquidate(LiquidateDTO.builder()
                .recordId(1759070380018892898L)
                .coin("bnb")
                .amount(new BigDecimal("0.043"))
                .build());
        log.info(bnb);

    }

    @Test
    void getStatusByHash() {
        fundIncomeTask.incomeTasks();
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