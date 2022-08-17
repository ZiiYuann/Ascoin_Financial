package com.tianli.currency.service;

import com.tianli.FinancialApplication;
import com.tianli.common.blockchain.CurrencyCoin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FinancialApplication.class)
public class CurrencyServiceTest {
    @Autowired
    private CurrencyService currencyService;

    @Test
    public void getDollarRateTest(){
        BigDecimal dollarRate = currencyService.getDollarRate(CurrencyCoin.bnb);
        System.out.printf(dollarRate.toString());
    }

}
