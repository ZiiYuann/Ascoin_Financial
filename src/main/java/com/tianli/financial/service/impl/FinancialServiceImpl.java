package com.tianli.financial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.account.entity.AccountBalance;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.*;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.*;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.vo.*;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

        // 生成一笔订单记录(进行中)
        Order order = Order.builder()
                .uid(uid)
                .coin(product.getCoin())
                .relatedId(product.getId())
                .orderNo(AccountChangeType.financial.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(amount)
                .type(ChargeType.purchase)
                .status(ChargeStatus.created)
                .createTime(LocalDateTime.now())
                .build();
        orderService.saveOrder(order);
        // 冻结余额
        accountBalanceService.freeze(uid, ChargeType.purchase, amount, order.getOrderNo(), CurrencyLogDes.申购.name());
        // 确认完毕后生成申购记录
        FinancialRecord financialRecord = financialRecordService.generateFinancialRecord(uid, product, amount);
        // 修改订单状态
        order.setStatus(ChargeStatus.chain_success);
        orderService.updateStatus(order);

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toVO(financialRecord);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setStatusDes(order.getStatus().name());
        return financialPurchaseResultVO;
    }

    @Override
    public IncomeVO income(Long uid) {
        List<ProductType> types = List.of(ProductType.values());

        BigDecimal totalHoldFee = BigDecimal.ZERO;
        BigDecimal totalAccrueIncomeFee = BigDecimal.ZERO;
        BigDecimal totalYesterdayIncomeFee = BigDecimal.ZERO;
        EnumMap<ProductType,IncomeVO> incomeMap = new EnumMap<>(ProductType.class);
        for(ProductType type : types){
            IncomeVO incomeVO = new IncomeVO();
            // 单类型产品持有币数量
            BigDecimal holdFee = financialRecordService.getPurchaseAmount(uid, type, RecordStatus.PROCESS);
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
    public List<HoldProductVo> myHold(Long uid, ProductType type) {

        List<FinancialRecord> financialRecords = financialRecordService.selectList(uid, type, RecordStatus.PROCESS);

        var productIds = financialRecords.stream().map(FinancialRecord:: getProductId).collect(Collectors.toList());
        var recordIds = financialRecords.stream().map(FinancialRecord:: getId).collect(Collectors.toList());

        var productMap = financialProductService.listByIds(productIds).stream()
                .collect(Collectors.toMap(FinancialProduct :: getId,o -> o));
        var accrueIncomeMap = accrueIncomeLogService.selectListByRecordId(recordIds).stream()
                .collect(Collectors.toMap(AccrueIncomeLog::getRecordId, o -> o));
        var dailyIncomeMap = dailyIncomeLogService.selectListByRecordId(uid,recordIds,requestInitService.yesterday()).stream()
                .collect(Collectors.toMap(DailyIncomeLog::getRecordId, o -> o));

        return financialRecords.stream().map(financialRecord ->{
            var holdProductVo = new HoldProductVo();
            var product = productMap.get(financialRecord.getProductId());
            var accrueIncomeLog = Optional.ofNullable(accrueIncomeMap.get(financialRecord.getId())).orElse(new AccrueIncomeLog());
            var dailyIncomeLog = Optional.ofNullable(dailyIncomeMap.get(financialRecord.getId())).orElse(new DailyIncomeLog());

            holdProductVo.setRecordId(financialRecord.getId());
            holdProductVo.setName(product.getName());
            holdProductVo.setRate(product.getRate());

            IncomeVO incomeVO = new IncomeVO();
            incomeVO.setHoldFee(financialRecord.getAmount());
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

    @Override
    public void validProduct(FinancialProduct financialProduct){
        if( Objects.isNull(financialProduct) || ProductStatus.open != financialProduct.getStatus()){
            ErrorCodeEnum.NOT_OPEN.throwException();
        }
    }

   @Override
    public void validRemainAmount(Long uid, CurrencyCoin currencyCoin, BigDecimal amount){
        AccountBalance accountBalanceBalance = accountBalanceService.getAndInit(uid,currencyCoin);
        if(accountBalanceBalance.getRemain().compareTo(amount) < 0){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    @Override
    public IPage<OrderFinancialVO> orderPage(Long uid, Page<OrderFinancialVO> page, ProductType productType, ChargeType chargeType) {
        return orderService.selectByPage(page,uid,productType,chargeType);
    }

    @Override
    public IPage<OrderFinancialVO> orderPage(Page<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery) {
        return orderService.selectByPage(page,financialOrdersQuery);
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
    private FinancialRecordService financialRecordService;
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
    @Resource
    private OrderService orderService;
}
