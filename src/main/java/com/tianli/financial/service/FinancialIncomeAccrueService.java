package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.IdGenerator;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.FinancialIncomeAccrueMapper;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Slf4j
@Service
public class FinancialIncomeAccrueService extends ServiceImpl<FinancialIncomeAccrueMapper, FinancialIncomeAccrue> {

    @Resource
    private FinancialIncomeAccrueMapper financialIncomeAccrueMapper;
    @Resource
    private CurrencyService currencyService;

    public IPage<FinancialIncomeAccrueDTO> incomeRecord(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query) {
        return financialIncomeAccrueMapper.pageByQuery(page, query);
    }

    public BigDecimal summaryIncomeByQuery(FinancialProductIncomeQuery query) {
        return financialIncomeAccrueMapper.summaryIncomeByQuery(query);
    }

    public List<FinancialIncomeAccrue> selectListByRecordId(List<Long> recordIds) {
        return financialIncomeAccrueMapper.selectList(new LambdaQueryWrapper<FinancialIncomeAccrue>()
                .in(FinancialIncomeAccrue::getRecordId, recordIds));
    }

    public FinancialIncomeAccrue selectByRecordId(Long uid,Long recordId) {
        return financialIncomeAccrueMapper.selectOne(new LambdaQueryWrapper<FinancialIncomeAccrue>()
                .eq(FinancialIncomeAccrue :: getUid,uid)
                .eq(FinancialIncomeAccrue::getRecordId, recordId));
    }

    @Transactional
    public void insertIncomeAccrue(Long uid, Long recordId, CurrencyCoin coin, BigDecimal amount){
        LambdaQueryWrapper<FinancialIncomeAccrue> queryWrapper = new LambdaQueryWrapper<FinancialIncomeAccrue>()
                .eq(FinancialIncomeAccrue::getUid, uid)
                .eq(FinancialIncomeAccrue::getRecordId, recordId);
        FinancialIncomeAccrue financialIncomeAccrue = financialIncomeAccrueMapper.selectOne(queryWrapper);
        LocalDateTime incomeTime = TimeTool.minDay(LocalDateTime.now()).plusDays(-1);

        if(Objects.nonNull(financialIncomeAccrue) && incomeTime.compareTo(financialIncomeAccrue.getUpdateTime()) == 0){
            log.error("recordId:{},已经计算过当日汇总利息，请排查问题",recordId);
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        if(Objects.isNull(financialIncomeAccrue)){
            financialIncomeAccrue = FinancialIncomeAccrue.builder().id(IdGenerator.financialIncomeAccrueId())
                    .coin(coin).uid(uid).recordId(recordId)
                    .incomeAmount(BigDecimal.ZERO)
                    .createTime(LocalDateTime.now())
                    .build();
            financialIncomeAccrueMapper.insert(financialIncomeAccrue);
        }

        BigDecimal incomeAmountOld = financialIncomeAccrue.getIncomeAmount();
        BigDecimal incomeAmountNew = incomeAmountOld.add(amount);
        financialIncomeAccrue.setIncomeAmount(incomeAmountNew);
        // incomeTime 为前一日整点
        financialIncomeAccrue.setUpdateTime(incomeTime);
        financialIncomeAccrueMapper.updateById(financialIncomeAccrue);
    }


    /**
     * 根据产品类型、状态获取利息总额
     *
     * @param uid  uid
     * @param type 产品类型
     */
    public BigDecimal getAccrueDollarAmount(Long uid, ProductType type) {


        List<FinancialIncomeAccrueDTO> accrueIncomeLogs = financialIncomeAccrueMapper.listByUidAndType(uid, type);

        if (CollectionUtils.isEmpty(accrueIncomeLogs)) {
            return BigDecimal.ZERO;
        }

        return accrueIncomeLogs.stream().map(log -> log.getIncomeAmount().multiply(currencyService.getDollarRate(log.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAmountSum(LocalDateTime endTime) {
        return financialIncomeAccrueMapper.getAmountSum(endTime);
    }

    /**
     * 获取不同用户盈亏信息
     */
    public Map<Long,BigDecimal> getSummaryAmount(List<Long> uids){
        var recordQuery = new LambdaQueryWrapper<FinancialIncomeAccrue>()
                .in(FinancialIncomeAccrue :: getUid,uids);

        var incomeMapByUid = Optional.ofNullable(financialIncomeAccrueMapper.selectList(recordQuery)).orElse(new ArrayList<>())
                .stream().collect(Collectors.groupingBy(FinancialIncomeAccrue::getUid));
        EnumMap<CurrencyCoin, BigDecimal> dollarRateMap = currencyService.getDollarRateMap();

        return incomeMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(income ->{
                    BigDecimal holdAmount = income.getIncomeAmount();
                    BigDecimal rate = dollarRateMap.getOrDefault(income.getCoin(), BigDecimal.ZERO);
                    return holdAmount.multiply(rate);
                }).reduce(BigDecimal.ZERO,BigDecimal::add)
        ));
    }
}
