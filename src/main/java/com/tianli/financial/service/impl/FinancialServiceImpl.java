package com.tianli.financial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.*;
import com.tianli.financial.vo.*;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
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
        validRemainAmount(uid,purchaseQuery.getCoin(),amount);

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
            BigDecimal yesterdayIncomeFee = financialIncomeDailyService.getYesterdayDailyAmount(uid,type);
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
                .collect(Collectors.toMap(FinancialIncomeAccrue::getRecordId, o -> o));
        var dailyIncomeMap = financialIncomeDailyService.selectListByRecordId(uid,recordIds,requestInitService.yesterday()).stream()
                .collect(Collectors.toMap(FinancialIncomeDaily::getRecordId, o -> o));

        return financialRecords.stream().map(financialRecord ->{
            var holdProductVo = new HoldProductVo();
            var product = productMap.get(financialRecord.getProductId());
            var accrueIncomeLog = Optional.ofNullable(accrueIncomeMap.get(financialRecord.getId())).orElse(new FinancialIncomeAccrue());
            var dailyIncomeLog = Optional.ofNullable(dailyIncomeMap.get(financialRecord.getId())).orElse(new FinancialIncomeDaily());

            holdProductVo.setRecordId(financialRecord.getId());
            holdProductVo.setName(product.getName());
            holdProductVo.setRate(product.getRate());
            holdProductVo.setProductType(product.getType());

            IncomeVO incomeVO = new IncomeVO();
            incomeVO.setHoldFee(financialRecord.getHoldAmount());
            incomeVO.setAccrueIncomeFee(Optional.ofNullable(accrueIncomeLog.getIncomeAmount()).orElse(BigDecimal.ZERO));
            incomeVO.setYesterdayIncomeFee(Optional.ofNullable(dailyIncomeLog.getIncomeAmount()).orElse(BigDecimal.ZERO));

            holdProductVo.setIncomeVO(incomeVO);
            return holdProductVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FinancialIncomeDailyVO> incomeDetails(Long uid, Long recordId) {
        FinancialRecord record = financialRecordService.getById(recordId);
        List<FinancialIncomeDaily> dailyIncomeLogs = financialIncomeDailyService.selectListByRecordId(uid, List.of(recordId), null);
        return dailyIncomeLogs.stream().map(income -> {
            FinancialIncomeDailyVO financialIncomeDailyVO = FinancialIncomeDailyVO.toVO(income);
            financialIncomeDailyVO.setCoin(record.getCoin());
            return financialIncomeDailyVO;
        }).collect(Collectors.toList());
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

    @Override
    public IPage<FinancialIncomeAccrueDTO> incomeRecord(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query) {
        return  accrueIncomeLogService.incomeRecord(page,query);
    }

    @Override
    public IPage<FinancialProductVO> products(Page<FinancialProduct> page, ProductType type) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open);

        return getFinancialProductVOIPage(page, type, query);

    }

    @Override
    public IPage<FinancialProductVO> activitiesProducts(Page<FinancialProduct> page, BusinessType type) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getBusinessType, ProductStatus.open);
        return getFinancialProductVOIPage(page, null, query);
    }

    private IPage<FinancialProductVO> getFinancialProductVOIPage(Page<FinancialProduct> page, ProductType type, LambdaQueryWrapper<FinancialProduct> query) {
        if(Objects.nonNull(type)){
            query.eq(FinancialProduct :: getType, type);
        }


        var list = financialProductService.page(page, query);
        List<Long> productId = list.getRecords().stream().map(FinancialProduct::getId).distinct().collect(Collectors.toList());

        Map<Long, BigDecimal> useQuotaMap = financialRecordService.getUseQuota(productId);
        Map<Long, BigDecimal> usePersonQuotaMap = financialRecordService.getUseQuota(productId, requestInitService.uid());
        return list.convert(product -> {
            BigDecimal useQuota = useQuotaMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            BigDecimal usePersonQuota = usePersonQuotaMap.getOrDefault(product.getId(), BigDecimal.ZERO);

            FinancialProductVO financialProductVO = financialConverter.toVO(product);
            financialProductVO.setUseQuota(useQuota);
            financialProductVO.setUserPersonQuota(usePersonQuota);

            if(usePersonQuota.compareTo(product.getPersonQuota()) < 0 || useQuota.compareTo(product.getTotalQuota()) < 0){
                financialProductVO.setAllowPurchase(true);
            }
            return financialProductVO;
        });
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

        FinancialIncomeDaily dailyIncomeLog = financialIncomeDailyService.getOne(new LambdaQueryWrapper<FinancialIncomeDaily>()
                .eq(FinancialIncomeDaily::getUid, uid)
                .eq(FinancialIncomeDaily::getRecordId, recordId)
                .eq(FinancialIncomeDaily::getFinishTime, yesterdayTime));

        if(Objects.nonNull(dailyIncomeLog)){
             log.error("申购记录ID：{}，重复计算每日利息，记息日时间：{}，程序运行时间：{}",recordId,yesterdayTime,now);
             return;
        }
        dailyIncomeLog = new FinancialIncomeDaily();
        dailyIncomeLog.setIncomeAmount(incomeFee);
        dailyIncomeLog.setId(CommonFunction.generalId());
        dailyIncomeLog.setUid(uid);
        dailyIncomeLog.setCreateTime(now);
        dailyIncomeLog.setFinishTime(yesterdayTime);
        financialIncomeDailyService.save(dailyIncomeLog);

        FinancialIncomeAccrue accrueIncome = getAccrueIncomeLogAndInit(uid, coin, recordId);
        accrueIncome.setIncomeAmount(accrueIncome.getIncomeAmount().add(incomeFee));
        accrueIncome.setUpdateTime(LocalDateTime.now());
        accrueIncomeLogService.updateById(accrueIncome);
    }

    private FinancialIncomeAccrue getAccrueIncomeLogAndInit(Long uid, CurrencyCoin coin, Long recordId){
        FinancialIncomeAccrue accrueIncomeLog = accrueIncomeLogService.getOne(new LambdaQueryWrapper<FinancialIncomeAccrue>()
                .eq(FinancialIncomeAccrue::getUid, uid)
                .eq(FinancialIncomeAccrue::getRecordId, recordId));
        if(Objects.isNull(accrueIncomeLog)){
            LocalDateTime now = LocalDateTime.now();
            accrueIncomeLog = FinancialIncomeAccrue.builder()
                    .id(CommonFunction.generalId())
                    .incomeAmount(BigDecimal.ZERO)
                    .recordId(recordId).uid(uid).createTime(now).updateTime(now)
                    .build();
            final FinancialIncomeAccrue accrueIncomeLogFinal = accrueIncomeLog;
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
    private FinancialIncomeDailyService financialIncomeDailyService;
    @Resource
    private FinancialIncomeAccrueService accrueIncomeLogService;
    @Resource
    private AsyncService asyncService;
    @Resource
    private OrderService orderService;

}
