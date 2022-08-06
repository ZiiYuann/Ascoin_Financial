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
//        tronTriggerContract.getStatusByHash("a4d9e15b137a1b1535cc04743861110c941395c7439859fcdda84592732bcd9d");
        bscTriggerContract.successByHash("0x265d355523f160668a18adeeb52637f9675bebfa96946260c145da8450c49206");
    }

}