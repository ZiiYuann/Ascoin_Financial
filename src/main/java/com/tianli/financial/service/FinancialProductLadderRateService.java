package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
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
import java.util.Objects;
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

            if (Objects.isNull(rates.get(i)) || Objects.isNull(rates.get(i).getRate())) {
                ErrorCodeEnum.throwException("不允许有null数据");
            }

            if (rates.get(i).getRate().compareTo(BigDecimal.ZERO) <= 0) {
                ErrorCodeEnum.throwException("利率不允许小于等于0");
            }

            if (i != rates.size() - 1 && (Objects.isNull(rates.get(i).getEndPoint()) || Objects.isNull(rates.get(i).getStartPoint()))) {
                ErrorCodeEnum.throwException("除了最后阶段结束节点，其余节点值不允许为空");
            }

            if (i == rates.size() - 1 && Objects.isNull(rates.get(i).getStartPoint())) {
                ErrorCodeEnum.throwException("最后一个阶段，开始节点不允许为空");
            }

            if (i == 0 && rates.get(i).getStartPoint().compareTo(BigDecimal.ZERO) != 0) {
                ErrorCodeEnum.throwException("第一阶段开始必须为0");
            }

            if (i != 0 && i != rates.size() - 1 && !rates.get(i).getEndPoint().equals(rates.get(i + 1).getStartPoint())) {
                ErrorCodeEnum.throwException("前一阶段的结束必须和后一阶段的开始相同");
            }

            if (i != 0 && !rates.get(i).getStartPoint().equals(rates.get(i - 1).getEndPoint())) {
                ErrorCodeEnum.throwException("前一阶段的结束必须和后一阶段的开始相同");
            }
        }


        var query =
                new LambdaQueryWrapper<FinancialProductLadderRate>().eq(FinancialProductLadderRate::getProductId, productId);
        baseMapper.delete(query);

        rates.forEach(rate -> {
            FinancialProductLadderRate financialProductLadderRate = financialConverter.toDO(rate);
            financialProductLadderRate.setId(CommonFunction.generalId());
            financialProductLadderRate.setProductId(productId);
            baseMapper.insert(financialProductLadderRate);
        });
    }

    public BigDecimal calLadderIncome(FinancialRecord record) {
        return calLadderIncome(record.getProductId(), record.getIncomeAmount());
    }

    /**
     * 计算阶梯利息
     *
     * @param productId 持有记录
     * @param incomeAmount 持有记录
     */
    public BigDecimal calLadderIncome(Long productId, BigDecimal incomeAmount) {

        List<BigDecimal> ladderIncomeAmounts = new ArrayList<>();
        List<FinancialProductLadderRate> ladderRates = this.listByProductId(productId);
        ladderRates.forEach(rate -> {

            if (incomeAmount.compareTo(rate.getStartPoint()) < 0) {
                return;
            }

            if (Objects.nonNull(rate.getEndPoint()) && incomeAmount.compareTo(rate.getEndPoint()) > 0) {
                BigDecimal ladderIncomeAmount = rate.getEndPoint().subtract(rate.getStartPoint()).multiply(rate.getRate()) // 乘年化利率
                        .divide(BigDecimal.valueOf(365), 12, RoundingMode.DOWN);
                ladderIncomeAmounts.add(ladderIncomeAmount);
            }

            // ( ]
            if (incomeAmount.compareTo(rate.getStartPoint()) >= 0
                    && (Objects.isNull(rate.getEndPoint()) || incomeAmount.compareTo(rate.getEndPoint()) <= 0)) {
                BigDecimal ladderIncomeAmount = incomeAmount.subtract(rate.getStartPoint()).multiply(rate.getRate()) // 乘年化利率
                        .divide(BigDecimal.valueOf(365), 12, RoundingMode.DOWN);
                ladderIncomeAmounts.add(ladderIncomeAmount);
            }
        });

        return ladderIncomeAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(8, RoundingMode.DOWN);

    }

    /**
     * 通过产品id获取数据
     */
    public List<FinancialProductLadderRate> listByProductId(Long productId) {
        var query =
                new LambdaQueryWrapper<FinancialProductLadderRate>().eq(FinancialProductLadderRate::getProductId, productId);
        return Optional.ofNullable(baseMapper.selectList(query)).orElse(new ArrayList<>());
    }
}
