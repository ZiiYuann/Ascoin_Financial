package com.tianli.financial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.common.CommonFunction;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.account.entity.AccountBalance;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.AccrueIncomeLog;
import com.tianli.financial.entity.DailyIncomeLog;
import com.tianli.financial.entity.FinancialPurchaseRecord;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.enums.FinancialProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.*;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.FinancialLogStatus;
import com.tianli.financial.vo.DailyIncomeLogVO;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.financial.vo.HoldProductVo;
import com.tianli.financial.vo.IncomeVO;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinancialServiceImpl implements FinancialService {

    @Override
    @Transactional
    public FinancialPurchaseResultVO purchase(PurchaseQuery purchaseQuery) {
        Long uid = requestInitService.uid();

        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());
        validProduct(product);

        BigDecimal amount = purchaseQuery.getAmount();
        validRemainAmount(purchaseQuery.getProductId(),purchaseQuery.getCurrencyCoin(),amount);

        Long id = CommonFunction.generalId();
        accountBalanceService.freeze(uid, AccountChangeType.financial, amount, id.toString(), CurrencyLogDes.申购.name());
        LocalDate startDate = requestInitService.now().toLocalDate().plusDays(1L);
        FinancialPurchaseRecord log = FinancialPurchaseRecord.builder()
                .productId(product.getId())
                .uid(uid).financialProductType(product.getType())
                .amount(amount)
                .createTime(requestInitService.now())
                .startDate(startDate)
                .endDate(startDate.plusDays(product.getPurchaseTerm().getDay()))
                .id(id).rate(product.getRate()).status(FinancialLogStatus.PURCHASE_PROCESSING.getType())
                .build();
        financialPurchaseRecordService.save(log);

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toVO(log);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setName(product.getNameEn());
        financialPurchaseResultVO.setStatusDes(FinancialLogStatus.getByType(log.getStatus()).getDesc());
        return financialPurchaseResultVO;
    }

    @Override
    public IncomeVO income(Long uid) {
        List<FinancialProductType> types = List.of(FinancialProductType.values());

        BigDecimal totalHoldFee = BigDecimal.ZERO;
        BigDecimal totalAccrueIncomeFee = BigDecimal.ZERO;
        BigDecimal totalYesterdayIncomeFee = BigDecimal.ZERO;
        Map<FinancialProductType,IncomeVO> incomeMap = new HashMap<>();
        for(FinancialProductType type : types){
            IncomeVO incomeVO = new IncomeVO();
            // 单类型产品持有币数量
            BigDecimal holdFee = financialPurchaseRecordService.getPurchaseAmount(uid, type, FinancialLogStatus.INTEREST_PROCESSING);
            incomeVO.setHoldFee(holdFee);
            totalHoldFee = totalHoldFee.add(holdFee);

            // 单类型产品累计收益
            BigDecimal incomeAmount = accrueIncomeLogService.getAccrueAmount(uid, type);
            incomeVO.setAccrueIncomeFee(incomeAmount);
            totalAccrueIncomeFee = totalAccrueIncomeFee.add(incomeAmount);

            // 单个类型产品昨日收益
            BigDecimal yesterdayIncomeFee = dailyIncomeLogService.getYesterdayDailyAmount(uid,type);
            incomeVO.setYesterdayIncomeFee(yesterdayIncomeFee);
            totalYesterdayIncomeFee = totalYesterdayIncomeFee.add(yesterdayIncomeFee);

            incomeMap.put(type,incomeVO);
        }


        IncomeVO incomeVO = new IncomeVO();
        incomeVO.setHoldFee(totalHoldFee);
        incomeVO.setAccrueIncomeFee(totalAccrueIncomeFee);
        incomeVO.setYesterdayIncomeFee(totalYesterdayIncomeFee);
        incomeVO.setIncomeMap(incomeMap);

        return incomeVO;
    }

    @Override
    public List<HoldProductVo> myHold(Long uid,FinancialProductType type) {

        List<FinancialPurchaseRecord> records = financialPurchaseRecordService.selectList(uid, type, FinancialLogStatus.INTEREST_PROCESSING);

        var productIds = records.stream().map(FinancialPurchaseRecord :: getProductId).collect(Collectors.toList());
        var recordIds = records.stream().map(FinancialPurchaseRecord :: getId).collect(Collectors.toList());

        var productMap = financialProductService.listByIds(productIds).stream()
                .collect(Collectors.toMap(FinancialProduct :: getId,o -> o));
        var accrueIncomeMap = accrueIncomeLogService.selectListByRecordId(recordIds).stream()
                .collect(Collectors.toMap(AccrueIncomeLog::getRecordId, o -> o));
        var dailyIncomeMap = dailyIncomeLogService.selectListByRecordId(uid,recordIds,requestInitService.yesterday()).stream()
                .collect(Collectors.toMap(DailyIncomeLog::getRecordId, o -> o));

        return records.stream().map(record ->{
            var holdProductVo = new HoldProductVo();
            var product = productMap.get(record.getProductId());
            var accrueIncomeLog = Optional.ofNullable(accrueIncomeMap.get(record.getId())).orElse(new AccrueIncomeLog());
            var dailyIncomeLog = Optional.ofNullable(dailyIncomeMap.get(record.getId())).orElse(new DailyIncomeLog());

            holdProductVo.setRecordId(record.getId());
            holdProductVo.setNameEn(product.getNameEn());
            holdProductVo.setName(product.getName());
            holdProductVo.setRate(product.getRate());

            IncomeVO incomeVO = new IncomeVO();
            incomeVO.setHoldFee(record.getAmount());
            incomeVO.setAccrueIncomeFee(Optional.ofNullable(accrueIncomeLog.getAccrueIncomeFee()).orElse(BigDecimal.ZERO));
            incomeVO.setYesterdayIncomeFee(Optional.ofNullable(dailyIncomeLog.getIncomeFee()).orElse(BigDecimal.ZERO));

            holdProductVo.setIncomeVO(incomeVO);
            return holdProductVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DailyIncomeLogVO> incomeDetails(Long uid, Long recordId) {
        List<DailyIncomeLog> dailyIncomeLogs = dailyIncomeLogService.selectListByRecordId(uid, List.of(recordId), null);
        return dailyIncomeLogs.stream().map(DailyIncomeLogVO :: toVO).collect(Collectors.toList());
    }

    /**
     * 校验产品是否处于开启状态
     * @param financialProduct 产品
     */
    private void validProduct(FinancialProduct financialProduct){
        if( Objects.isNull(financialProduct) || FinancialProductStatus.enable != financialProduct.getStatus()){
            ErrorCodeEnum.NOT_OPEN.throwException();
        }
    }

    /**
     * 校验账户额度
     * @param amount 申购金额
     */
    private void validRemainAmount(Long uid, CurrencyCoin currencyCoin, BigDecimal amount){
        AccountBalance accountBalanceBalance = accountBalanceService.getAndInit(uid,currencyCoin);
        if(accountBalanceBalance.getRemain().compareTo(amount) < 0){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    /**
     * 计算每日利息
     */
    @Transactional
    public void calculateDailyIncome(){
        // 前日零点
        LocalDateTime now = requestInitService.now();
        LocalDateTime yesterdayTime = requestInitService.yesterday();


        Long uid = 0L;
        Long recordId = 0L;
        CurrencyCoin coin = null;
        BigDecimal incomeFee = null;

        DailyIncomeLog dailyIncomeLog = dailyIncomeLogService.getOne(new LambdaQueryWrapper<DailyIncomeLog>()
                .eq(DailyIncomeLog::getUid, uid)
                .eq(DailyIncomeLog::getRecordId, recordId)
                .eq(DailyIncomeLog::getFinishTime, yesterdayTime));

        if(Objects.nonNull(dailyIncomeLog)){
             log.error("申购记录ID：{}，重复计算每日利息，记息日时间：{}，程序运行时间：{}",recordId,yesterdayTime,now);
             return;
        }
        dailyIncomeLog = new DailyIncomeLog();
        dailyIncomeLog.setIncomeFee(incomeFee);
        dailyIncomeLog.setCoin(coin);
        dailyIncomeLog.setId(CommonFunction.generalId());
        dailyIncomeLog.setUid(uid);
        dailyIncomeLog.setCreateTime(now);
        dailyIncomeLog.setFinishTime(yesterdayTime);
        dailyIncomeLogService.save(dailyIncomeLog);

        AccrueIncomeLog accrueIncome = getAccrueIncomeLogAndInit(uid, coin, recordId);
        accrueIncome.setAccrueIncomeFee(accrueIncome.getAccrueIncomeFee().add(incomeFee));
        accrueIncome.setUpdateTime(LocalDateTime.now());
        accrueIncomeLogService.updateById(accrueIncome);
    }

    private AccrueIncomeLog getAccrueIncomeLogAndInit(Long uid,CurrencyCoin coin,Long recordId){
        AccrueIncomeLog accrueIncomeLog = accrueIncomeLogService.getOne(new LambdaQueryWrapper<AccrueIncomeLog>()
                .eq(AccrueIncomeLog::getUid, uid)
                .eq(AccrueIncomeLog::getRecordId, recordId));
        if(Objects.isNull(accrueIncomeLog)){
            LocalDateTime now = LocalDateTime.now();
            accrueIncomeLog = AccrueIncomeLog.builder()
                    .id(CommonFunction.generalId())
                    .accrueIncomeFee(BigDecimal.ZERO)
                    .recordId(recordId).uid(uid).createTime(now).updateTime(now).coin(coin)
                    .build();
            final  AccrueIncomeLog accrueIncomeLogFinal = accrueIncomeLog;
            asyncService.async(() -> accrueIncomeLogService.save(accrueIncomeLogFinal));
        }
        return accrueIncomeLog;
    }

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialPurchaseRecordService financialPurchaseRecordService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private DailyIncomeLogService dailyIncomeLogService;
    @Resource
    private AccrueIncomeLogService accrueIncomeLogService;
    @Resource
    private AsyncService asyncService;
}
