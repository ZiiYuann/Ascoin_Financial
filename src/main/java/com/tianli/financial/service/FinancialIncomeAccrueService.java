package com.tianli.financial.service;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.service.CurrencyService;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.FinancialIncomeAccrueMapper;
import com.tianli.management.query.FinancialProductIncomeQuery;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Service
public class FinancialIncomeAccrueService extends ServiceImpl<FinancialIncomeAccrueMapper, FinancialIncomeAccrue> {

    @Resource
    private FinancialIncomeAccrueMapper financialIncomeAccrueMapper;
    @Resource
    private CurrencyService currencyService;

    public IPage<FinancialIncomeAccrueDTO> incomeRecord(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query) {
        return financialIncomeAccrueMapper.pageByQuery(page, query);
    }

    public List<FinancialIncomeAccrue> selectListByRecordId(List<Long> recordIds) {
        return financialIncomeAccrueMapper.selectList(new LambdaQueryWrapper<FinancialIncomeAccrue>()
                .in(FinancialIncomeAccrue::getRecordId, recordIds));
    }



    /**
     * 根据产品类型、状态获取利息总额
     *
     * @param uid  uid
     * @param type 产品类型
     */
    public BigDecimal getAccrueAmount(Long uid, ProductType type) {


        List<FinancialIncomeAccrueDTO> accrueIncomeLogs = financialIncomeAccrueMapper.listByUidAndType(uid, type);

        if (CollectionUtils.isEmpty(accrueIncomeLogs)) {
            return BigDecimal.ZERO;
        }

        return accrueIncomeLogs.stream().map(log -> log.getIncomeAmount().multiply(currencyService.getDollarRate(log.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
