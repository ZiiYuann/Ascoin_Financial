package com.tianli.task;

import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.core.tool.PrivateAccessor;
import com.tianli.CommonConstant;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.*;
import com.tianli.product.service.FinancialProductService;
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

        @MockInvoke(scope = MockScope.ASSOCIATED)
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

        @MockInvoke(scope = MockScope.ASSOCIATED)
        public void insertIncomeAccrue(FinancialIncomeAccrueService self, Long uid, Long recordId,
                                       String coin, BigDecimal amount) {
        }

        @MockInvoke(scope = MockScope.ASSOCIATED)
        public void insertIncomeDaily(FinancialIncomeDailyService self, Long uid, Long recordId,
                                      BigDecimal amount) {
        }

        @MockInvoke(scope = MockScope.ASSOCIATED)
        public void increase(AccountBalanceService self, long uid, ChargeType type,
                             String coin, BigDecimal amount,
                             String sn, String des) {
        }

        @MockInvoke(scope = MockScope.ASSOCIATED)
        public void increaseIncomeAmount(FinancialRecordService self, Long recordId, BigDecimal amount,
                                         BigDecimal originalAmount) {
        log.info("增加计息金额为：【{}】,原先计息金额为：【{}】",amount,originalAmount);
        }

        @MockInvoke(scope = MockScope.ASSOCIATED)
        public boolean save(OrderService self,Order order) {
            return false;
        }

        @MockInvoke(scope = MockScope.ASSOCIATED)
        public BigDecimal calLadderIncome(FinancialProductLadderRateService self, FinancialRecord record) {
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
