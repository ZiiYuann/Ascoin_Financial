package com.tianli.chain.service.contract;

import com.tianli.FinancialApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FinancialApplication.class)
class TronTriggerContractTest {

    @Resource
    private TronTriggerContract tronTriggerContract;

    @Resource
    private BscTriggerContract bscTriggerContract;

    @Test
    void getStatusByHash() {
        tronTriggerContract.successByHash("00d704b30379b7137fd0b5a96531a8eea7e077b68a39513e026c006083bd5cfd");
    }

}