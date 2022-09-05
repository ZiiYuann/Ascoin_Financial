package com.tianli.financial.service;

import com.alibaba.testable.core.annotation.MockInvoke;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.tianli.CommonConstant;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProductLadderRate;
import com.tianli.financial.mapper.FinancialProductLadderRateMapper;
import com.tianli.management.query.FinancialProductLadderRateIoUQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinancialProductLadderRateServiceTest {

    private final FinancialProductLadderRateService service = new FinancialProductLadderRateService();

    public static class Mock {
        @MockInvoke
        @SuppressWarnings("unchecked")
        public <T> List<T> selectList(FinancialProductLadderRateMapper self, Wrapper<T> queryWrapper) {
            return (List<T>) CommonConstant.ladderRates;
        }

        @MockInvoke
        public <T> int delete(FinancialProductLadderRateMapper self, Wrapper<T> wrapper) {
            return 0;
        }

        @MockInvoke
        public FinancialProductLadderRate toDO(
                FinancialConverter self, FinancialProductLadderRateIoUQuery financialProductLadderRateIoUQuery) {
            return new FinancialProductLadderRate();
        }

        @MockInvoke
        public <T> int insert(FinancialProductLadderRateMapper self, T entity) {
            return 0;
        }
    }


    @Test
    public void insert() {
        List<FinancialProductLadderRateIoUQuery> ladderRates = new ArrayList<>();
        ladderRates.add(FinancialProductLadderRateIoUQuery.builder()
                .startPoint(BigDecimal.ZERO)
                .endPoint(BigDecimal.valueOf(1000L))
                .rate(BigDecimal.valueOf(0.1))
                .build());

        ladderRates.add(FinancialProductLadderRateIoUQuery.builder()
                .startPoint(BigDecimal.valueOf(1000L))
                .endPoint(BigDecimal.valueOf(3000L))
                .rate(BigDecimal.valueOf(0.3))
                .build());

        ladderRates.add(FinancialProductLadderRateIoUQuery.builder()
                .startPoint(BigDecimal.valueOf(3000L))
                .endPoint(BigDecimal.valueOf(10000L))
                .rate(BigDecimal.valueOf(0.5))
                .build());
        service.insert(1L,ladderRates);
    }

}
