package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.IdGenerator;
import com.tianli.common.TimeUtils;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.service.CurrencyService;
import com.tianli.financial.dto.FinancialIncomeDailyDTO;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.FinancialIncomeDailyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Slf4j
@Service
public class FinancialIncomeDailyService extends ServiceImpl<FinancialIncomeDailyMapper, FinancialIncomeDaily> {

    @Resource
    private FinancialIncomeDailyMapper financialIncomeDailyMapper;
    @Resource
    private CurrencyService currencyService;

    /**
     * 根据产品类型、状态获取利息总额
     * @param uid uid
     * @param type 产品类型
     */
    public BigDecimal getYesterdayDailyAmount(Long uid, ProductType type){

        LocalDateTime todayZero = TimeUtils.StartOfTime(TimeUtils.Util.DAY);
        LocalDateTime yesterdayZero = todayZero.plusDays(-1);

        List<FinancialIncomeDailyDTO> dailyIncomeLogs = financialIncomeDailyMapper.listByUidAndType(uid,type,yesterdayZero,todayZero);

        if(CollectionUtils.isEmpty(dailyIncomeLogs)){
            return BigDecimal.ZERO;
        }

        return dailyIncomeLogs.stream().map(log-> log.getIncomeAmount().multiply(currencyService.getDollarRate(log.getCoin())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    @Transactional
    public void insertIncomeDaily(Long uid, Long recordId, BigDecimal amount){
        LocalDateTime yesterdayZero = TimeUtils.StartOfTime(TimeUtils.Util.DAY).plusDays(-1);
        LambdaQueryWrapper<FinancialIncomeDaily> queryWrapper = new LambdaQueryWrapper<FinancialIncomeDaily>()
                .eq(FinancialIncomeDaily::getUid, uid)
                .eq(FinancialIncomeDaily::getRecordId, recordId)
                .eq(FinancialIncomeDaily::getFinishTime,yesterdayZero);
        FinancialIncomeDaily incomeDaily = financialIncomeDailyMapper.selectOne(queryWrapper);
        if(Objects.nonNull(incomeDaily)){
            log.error("recordId:{},已经计算过当日利息，请排查问题",recordId);
            return;
        }
        FinancialIncomeDaily incomeDailyInsert = FinancialIncomeDaily.builder()
                .id(IdGenerator.financialIncomeDailyId())
                .uid(uid).recordId(recordId).incomeAmount(amount)
                .createTime(LocalDateTime.now()).finishTime(yesterdayZero)
                .build();
        financialIncomeDailyMapper.insert(incomeDailyInsert);
    }

    public List<FinancialIncomeDaily> selectListByRecordIds(Long uid, List<Long> recordIds, LocalDateTime finishTime){
        LambdaQueryWrapper<FinancialIncomeDaily> query = new LambdaQueryWrapper<FinancialIncomeDaily>()
                .eq(FinancialIncomeDaily::getUid, uid)
                .in(FinancialIncomeDaily::getRecordId, recordIds)
                .orderByDesc( FinancialIncomeDaily:: getFinishTime);
        if(Objects.nonNull(finishTime)){
           query = query.eq(FinancialIncomeDaily:: getFinishTime,finishTime);
        }
        return financialIncomeDailyMapper.selectList(query);
    }

    public IPage<FinancialIncomeDaily> pageByRecordId(IPage<FinancialIncomeDaily> page,Long uid, List<Long> recordIds, LocalDateTime finishTime){
        LambdaQueryWrapper<FinancialIncomeDaily> query = new LambdaQueryWrapper<FinancialIncomeDaily>()
                .eq(FinancialIncomeDaily::getUid, uid)
                .in(FinancialIncomeDaily::getRecordId, recordIds)
                .orderByDesc( FinancialIncomeDaily:: getFinishTime);
        if(Objects.nonNull(finishTime)){
           query = query.eq(FinancialIncomeDaily:: getFinishTime,finishTime);
        }
        return financialIncomeDailyMapper.selectPage(page,query);
    }
}