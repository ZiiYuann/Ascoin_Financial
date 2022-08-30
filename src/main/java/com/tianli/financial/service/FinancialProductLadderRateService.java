package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProductLadderRate;
import com.tianli.financial.mapper.FinancialProductLadderRateMapper;
import com.tianli.management.query.FinancialProductLadderRateIoUQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
    public void insert(Long productId, List<FinancialProductLadderRateIoUQuery> rates){
        if(CollectionUtils.isEmpty(rates)){
            return;
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
}
