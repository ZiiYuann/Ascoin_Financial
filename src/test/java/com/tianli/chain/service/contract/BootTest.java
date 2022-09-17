package com.tianli.chain.service.contract;

import com.alibaba.testable.core.annotation.MockInvoke;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.FinancialApplication;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.task.FinancialIncomeTask;
import com.tianli.task.FundIncomeTask;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FinancialApplication.class)

class BootTest {

    @Resource
    private TronTriggerContract tronTriggerContract;

    @Resource
    private FundIncomeTask fundIncomeTask;
    @Resource
    private FinancialIncomeTask financialIncomeTask;
    @Resource
    private FinancialRecordService financialRecordService;

    @Test
    void getStatusByHash() {
        fundIncomeTask.incomeTasks();
    }

    @Test
    void income() throws InterruptedException {
        while (true){
            List<FinancialRecord> records = financialRecordService.needCalIncomeRecord(new Page<>(1, 100)).getRecords();

            records.forEach( record -> financialIncomeTask.interestStat(record));
            Thread.sleep(1000);

        }
    }

}