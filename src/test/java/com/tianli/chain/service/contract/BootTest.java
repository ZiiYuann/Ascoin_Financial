package com.tianli.chain.service.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.FinancialApplication;
import com.tianli.common.RedisConstants;
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
import org.tron.tronj.abi.datatypes.Address;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.annotation.Resource;
import java.io.IOException;
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
    private JsonRpc2_0Web3j ethWeb3j;
    @Resource
    private TronWeb3jContract tronWeb3jContract;


    @Test
    void getStatusByHash() throws IOException {
        var ethTriggerContract =
                bscTriggerContract.getTransactionReceipt("0x4c7ad65c3690ab9cfb357ed7ea7d9c8a5667e97654617113c81b2272dd50c2b5");

        log.info("1111");
    }

    public static void main(String[] args) {
        Address address = new Address("0x000000000000000000000000a54d6cb7f0c2a3deb29f29e983049662a6f0f8ac");
        log.info("1111");
    }

    @Test
    void getStatusByHash1() throws IOException {
        TransactionReceipt transactionReceipt =
                bscTriggerContract.getTransactionReceipt("0x4c7ad65c3690ab9cfb357ed7ea7d9c8a5667e97654617113c81b2272dd50c2b5");
        log.info("1111");
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