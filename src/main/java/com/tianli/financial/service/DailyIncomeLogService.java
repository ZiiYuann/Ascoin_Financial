package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.service.CurrencyService;
import com.tianli.financial.entity.AccrueIncomeLog;
import com.tianli.financial.entity.DailyIncomeLog;
import com.tianli.financial.enums.FinancialProductType;
import com.tianli.financial.mapper.DailyIncomeLogMapper;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Service
public class DailyIncomeLogService extends ServiceImpl<DailyIncomeLogMapper, DailyIncomeLog> {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private DailyIncomeLogMapper dailyIncomeLogMapper;
    @Resource
    private CurrencyService currencyService;

    /**
     * 根据产品类型、状态获取利息总额
     * @param uid uid
     * @param type 产品类型
     */
    public BigDecimal getYesterdayDailyAmount(Long uid, FinancialProductType type){
        LambdaQueryWrapper<DailyIncomeLog> query = new LambdaQueryWrapper<DailyIncomeLog>()
                .eq(DailyIncomeLog::getUid, uid)
                .eq(DailyIncomeLog::getFinancialProductType, type)
                .eq(DailyIncomeLog :: getFinishTime, requestInitService.yesterday());


        List<DailyIncomeLog> dailyIncomeLogs = dailyIncomeLogMapper.selectList(query);

        if(CollectionUtils.isEmpty(dailyIncomeLogs)){
            return BigDecimal.ZERO;
        }

        return dailyIncomeLogs.stream().map(log-> log.getIncomeFee().multiply(currencyService.getDollarRate(log.getCoin())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public List<DailyIncomeLog> selectListByRecordId(Long uid,List<Long> recordIds,LocalDateTime finishTime){
        LambdaQueryWrapper<DailyIncomeLog> query = new LambdaQueryWrapper<DailyIncomeLog>()
                .eq(DailyIncomeLog::getUid, uid)
                .in(DailyIncomeLog::getRecordId, recordIds)
                .orderByDesc( DailyIncomeLog :: getFinishTime);
        if(Objects.nonNull(finishTime)){
           query = query.eq(DailyIncomeLog :: getFinishTime,finishTime);
        }
        return dailyIncomeLogMapper.selectList(query);
    }
}
