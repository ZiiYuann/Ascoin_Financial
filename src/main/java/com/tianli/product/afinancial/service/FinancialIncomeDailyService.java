package com.tianli.product.afinancial.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.IdGenerator;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.afinancial.dto.FinancialIncomeDailyDTO;
import com.tianli.product.afinancial.entity.FinancialIncomeDaily;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.mapper.FinancialIncomeDailyMapper;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.service.IFundRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
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
    @Resource
    private IFundRecordService fundRecordService;

    /**
     * 根据产品类型、状态获取利息总额
     *
     * @param uid  uid
     * @param type 产品类型
     */
    public BigDecimal amountDollarYesterday(Long uid, ProductType type) {

        LocalDateTime todayZero = DateUtil.beginOfDay(new Date()).toLocalDateTime();
        LocalDateTime yesterdayZero = todayZero.plusDays(-1);

        List<FinancialIncomeDailyDTO> dailyIncomeLogs = financialIncomeDailyMapper.listByUidAndType(uid, type
                , null, yesterdayZero, todayZero);

        if (CollectionUtils.isEmpty(dailyIncomeLogs)) {
            return BigDecimal.ZERO;
        }

        return dailyIncomeLogs.stream().map(log -> log.getIncomeAmount().multiply(currencyService.getDollarRate(log.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal amountDollarYesterday(Long recordId) {

        FundRecord fundRecord = fundRecordService.getById(recordId);
        return this.amountYesterday(recordId).multiply(currencyService.getDollarRate(fundRecord.getCoin()));

    }

    public BigDecimal amountYesterday(Long recordId) {

        LocalDateTime todayZero = DateUtil.beginOfDay(new Date()).toLocalDateTime();
        LocalDateTime yesterdayZero = todayZero.plusDays(-1);

        List<FinancialIncomeDailyDTO> dailyIncomeLogs = financialIncomeDailyMapper.listByUidAndType(null, null
                , recordId, yesterdayZero, todayZero);

        if (CollectionUtils.isEmpty(dailyIncomeLogs)) {
            return BigDecimal.ZERO;
        }

        return dailyIncomeLogs.stream().map(FinancialIncomeDaily::getIncomeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void insertIncomeDaily(Long uid, Long recordId, BigDecimal amount, BigDecimal calIncomeAmount,
                                  BigDecimal rate, String orderNo,LocalDateTime incomeTime) {
        LocalDateTime incomeYesterdayZero = incomeTime.toLocalDate().atStartOfDay().plusDays(-1);
        LambdaQueryWrapper<FinancialIncomeDaily> queryWrapper = new LambdaQueryWrapper<FinancialIncomeDaily>()
                .eq(FinancialIncomeDaily::getUid, uid)
                .eq(FinancialIncomeDaily::getRecordId, recordId)
                .eq(FinancialIncomeDaily::getFinishTime, incomeYesterdayZero);
        FinancialIncomeDaily incomeDaily = financialIncomeDailyMapper.selectOne(queryWrapper);
        if (Objects.nonNull(incomeDaily)) {
            ErrorCodeEnum.FINANCIAL_INCOME_REPEAT.throwException();
        }
        FinancialIncomeDaily incomeDailyInsert = FinancialIncomeDaily.builder()
                .id(IdGenerator.financialIncomeDailyId())
                .uid(uid).recordId(recordId).incomeAmount(amount)
                .createTime(LocalDateTime.now()).finishTime(incomeYesterdayZero)
                .orderNo(orderNo).rate(rate).amount(calIncomeAmount)
                .build();
        financialIncomeDailyMapper.insert(incomeDailyInsert);
    }

    public IPage<FinancialIncomeDaily> pageByRecordId(IPage<FinancialIncomeDaily> page, Long uid, List<Long> recordIds, LocalDateTime finishTime) {
        LambdaQueryWrapper<FinancialIncomeDaily> query = new LambdaQueryWrapper<FinancialIncomeDaily>()
                .eq(FinancialIncomeDaily::getUid, uid)
                .in(FinancialIncomeDaily::getRecordId, recordIds)
                .orderByDesc(FinancialIncomeDaily::getFinishTime);
        if (Objects.nonNull(finishTime)) {
            query = query.eq(FinancialIncomeDaily::getFinishTime, finishTime);
        }
        return financialIncomeDailyMapper.selectPage(page, query);
    }


}
