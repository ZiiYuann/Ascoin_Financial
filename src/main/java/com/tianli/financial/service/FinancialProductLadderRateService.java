package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialProductLadderRate;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.mapper.FinancialProductLadderRateMapper;
import com.tianli.management.query.FinancialProductLadderRateIoUQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-30
 **/
@Service
public class FinancialProductLadderRateService extends ServiceImpl<FinancialProductLadderRateMapper, FinancialProductLadderRate> {

    @Resource
    private FinancialConverter financialConverter;

    @Transactional
    public void insert(Long productId, List<FinancialProductLadderRateIoUQuery> rates) {
        if (CollectionUtils.isEmpty(rates)) {
            return;
        }

        for (int i = 0; i < rates.size(); i++) {
            if (i == 0 && rates.get(i).getStartPoint().compareTo(BigDecimal.ZERO) != 0) {
                ErrorCodeEnum.throwException("第一阶段开始必须为0");
            }

            if (i != 0 && i != rates.size() - 1 && !rates.get(i).getEndPoint().equals(rates.get(i+1).getStartPoint()) ) {
                ErrorCodeEnum.throwException("前一阶段的结束必须和后一阶段的开始相同");
            }
        }


        var query =
                new LambdaQueryWrapper<FinancialProductLadderRate>().eq(FinancialProductLadderRate::getProductId, productId);
        baseMapper.delete(query);

        rates.forEach(rate -> {
            FinancialProductLadderRate financialProductLadderRate = financialConverter.toDO(rate);
            financialProductLadderRate.setId(CommonFunction.generalId());
            baseMapper.insert(financialProductLadderRate);
        });
    }


    /**
     * 计算阶梯利息
     *
     * @param productId 产品id
     * @param record    持有记录
     * @return
     */
    public BigDecimal calLadderIncome(FinancialRecord record) {

        BigDecimal incomeAmount = record.getIncomeAmount();
        List<BigDecimal> ladderIncomeAmounts = new ArrayList<>();
        var query =
                new LambdaQueryWrapper<FinancialProductLadderRate>().eq(FinancialProductLadderRate::getProductId, record.getProductId());
        List<FinancialProductLadderRate> ladderRates = Optional.ofNullable(baseMapper.selectList(query)).orElse(new ArrayList<>());

        ladderRates.forEach( rate ->{
            if (incomeAmount.compareTo(rate.getStartPoint()) < 0){
                return;
            }

            if (incomeAmount.compareTo(rate.getEndPoint()) > 0){
                BigDecimal ladderIncomeAmount = rate.getEndPoint().subtract(rate.getStartPoint()).multiply(rate.getRate()) // 乘年化利率
                        .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN);
                ladderIncomeAmounts.add(ladderIncomeAmount);
            }

            // ( ]
            if (incomeAmount.compareTo(rate.getStartPoint()) >= 0 && incomeAmount.compareTo(rate.getEndPoint()) <= 0){
                BigDecimal ladderIncomeAmount = incomeAmount.subtract(rate.getStartPoint()).multiply(rate.getRate()) // 乘年化利率
                        .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN);
                ladderIncomeAmounts.add(ladderIncomeAmount);
            }
        });

        return ladderIncomeAmounts.stream().reduce(BigDecimal.ZERO,BigDecimal::add);

    }
}
