package com.tianli.chain.service.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.FinancialApplication;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.task.FinancialIncomeTask;
import com.tianli.task.FundIncomeTask;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FinancialApplication.class)
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
    private TronTriggerContract tronTriggerContract;

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
    void contract() throws InterruptedException {
        boolean success = tronTriggerContract.successByHash("8d85c058340a602f08751361a5ca7aaa91871ade2812c86cab4c50e2ba6c05d5");
    }




}