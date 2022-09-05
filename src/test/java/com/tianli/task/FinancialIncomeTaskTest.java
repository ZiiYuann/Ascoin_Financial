package com.tianli.task;

import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.tool.PrivateAccessor;
import com.tianli.CommonConstant;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class FinancialIncomeTaskTest {

    private FinancialIncomeTask financialIncomeTask = new FinancialIncomeTask();

    public static class Mock {

        @MockInvoke
        @SuppressWarnings("unchecked")
        private <T> T getById(FinancialProductService self, Serializable id) {
            FinancialProduct.FinancialProductBuilder builder = FinancialProduct.builder();
            // 默认所有mock类利率都是10
            builder.rate(CommonConstant.DEFAULT_RATE);
            switch (id.toString()) {
                case CommonConstant.PRODUCT_ONE:
                    builder.rateType((byte) 0); break;
                case CommonConstant.PRODUCT_TWO:
                    builder.rateType((byte) 1); break;
            }

            return (T) builder.build();
        }

        @MockInvoke
        public void insertIncomeAccrue(FinancialIncomeAccrueService self, Long uid, Long recordId,
                                       CurrencyCoin coin, BigDecimal amount) {
        }

        @MockInvoke
        public void insertIncomeDaily(FinancialIncomeDailyService self, Long uid, Long recordId,
                                      BigDecimal amount) {
        }

        @MockInvoke
        public void increase(AccountBalanceService self, long uid, ChargeType type,
                             CurrencyCoin coin, BigDecimal amount,
                             String sn, String des) {
        }

        @MockInvoke
        public void increaseIncomeAmount(FinancialRecordService self, Long recordId, BigDecimal amount,
                                         BigDecimal originalAmount) {
        log.info("增加计息金额为：【{}】,原先计息金额为：【{}】",amount,originalAmount);
        }

        @MockInvoke
        public boolean save(OrderService self,Order order) {
            return false;
        }

        @MockInvoke
        public BigDecimal calLadderIncome(FinancialProductLadderRateService self,FinancialRecord record) {
            self = new FinancialProductLadderRateService();
            return self.calLadderIncome(record);
        }
    }


    /**
     * 普通计算利息
     */
    @Test
    public void incomeOperation() {
        Order order = PrivateAccessor.invoke(financialIncomeTask, "incomeOperation", CommonConstant.record1, LocalDateTime.now());
        assertEquals(BigDecimal.valueOf(0.00136986),order.getAmount());

    }

    /**
     * 阶梯计算利息
     */
    @Test
    public void incomeOperationCalLadder() {
        Order order = PrivateAccessor.invoke(financialIncomeTask, "incomeOperation", CommonConstant.record2, LocalDateTime.now());
        assertEquals(BigDecimal.valueOf(4.65753424),order.getAmount());
    }



}
