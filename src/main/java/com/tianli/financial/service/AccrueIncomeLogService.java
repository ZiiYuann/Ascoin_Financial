package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.service.CurrencyService;
import com.tianli.financial.entity.AccrueIncomeLog;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.AccrueIncomeLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Service
public class AccrueIncomeLogService extends ServiceImpl<AccrueIncomeLogMapper, AccrueIncomeLog> {

    @Resource
    private AccrueIncomeLogMapper AccrueIncomeLogMapper;
    @Resource
    private CurrencyService currencyService;

    public List<AccrueIncomeLog> selectListByUid(Long uid){
        return AccrueIncomeLogMapper.selectListByUid(uid);
    }

    public List<AccrueIncomeLog> selectListByRecordId(List<Long> recordIds){
        return AccrueIncomeLogMapper.selectList(new LambdaQueryWrapper<AccrueIncomeLog>()
                .in(AccrueIncomeLog :: getRecordId,recordIds));
    }

    /**
     * 根据产品类型、状态获取利息总额
     * @param uid uid
     * @param type 产品类型
     */
    public BigDecimal getAccrueAmount(Long uid, ProductType type){
        LambdaQueryWrapper<AccrueIncomeLog> query = new LambdaQueryWrapper<AccrueIncomeLog>()
                .eq(AccrueIncomeLog::getUid, uid)
                .eq(AccrueIncomeLog::getFinancialProductType, type);

        List<AccrueIncomeLog> accrueIncomeLogs = AccrueIncomeLogMapper.selectList(query);

        if(CollectionUtils.isEmpty(accrueIncomeLogs)){
            return BigDecimal.ZERO;
        }

       return accrueIncomeLogs.stream().map(log-> log.getAccrueIncomeFee().multiply(currencyService.getDollarRate(log.getCoin())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }


}
