package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.mapper.FinancialProductMapper;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.management.query.FinancialProductsQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class FinancialProductService extends ServiceImpl<FinancialProductMapper, FinancialProduct> {

    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private FinancialProductMapper financialProductMapper;

    /**
     * 查询产品列表数据
     */
    public IPage<FinancialProductVO> selectListByQuery(IPage<FinancialProduct> page, FinancialProductsQuery query){
        LambdaQueryWrapper<FinancialProduct> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(query.getName())){
            queryWrapper = queryWrapper.like(FinancialProduct :: getName,query.getName());
        }
        if(Objects.nonNull(query.getType())){
            queryWrapper = queryWrapper.eq(FinancialProduct :: getType,query.getType());
        }
        if(Objects.nonNull(query.getStatus())){
            queryWrapper = queryWrapper.eq(FinancialProduct :: getStatus,query.getStatus());
        }

        queryWrapper = queryWrapper.orderByDesc(FinancialProduct :: getCreateTime);

        IPage<FinancialProduct> financialProductIPage = financialProductMapper.selectPage(page, queryWrapper);
        return financialProductIPage.convert(financialConverter :: toVO);
    }
}
