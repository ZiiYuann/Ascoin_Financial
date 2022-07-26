package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.TimeUtils;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.FinancialRecordMapper;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FinancialRecordService extends ServiceImpl<FinancialRecordMapper, FinancialRecord> {

    @Resource
    private FinancialRecordMapper financialRecordMapper;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CurrencyService currencyService;

    /**
     * 赎回金额
     */
    @Transactional
    public void redeem(Long recordId,BigDecimal redeemAmount){
        if(financialRecordMapper.reduce(recordId,redeemAmount,LocalDateTime.now()) < 0){
            log.error("赎回异常，recordId:{},amount:{}",recordId,redeemAmount);
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    /**
     * 默认通过id查询需要带上uid
     */
    public FinancialRecord selectById(Long recordId,Long uid){
        if(Objects.isNull(recordId) || Objects.isNull(uid)){
            ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
        }

        LambdaQueryWrapper<FinancialRecord> query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getId, recordId).eq(FinancialRecord::getUid, uid);

     return Optional.ofNullable(financialRecordMapper.selectOne(query)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR :: generalException);
    }

    public List<FinancialRecord> selectByProductId(Long productId){
        if(Objects.isNull(productId)){
            ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
        }

        LambdaQueryWrapper<FinancialRecord> query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getProductId, productId);

     return Optional.ofNullable(financialRecordMapper.selectList(query)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR :: generalException);
    }

    /**
     * 获取不同产品已经使用的总额度
     */
    public Map<Long,BigDecimal> getUseQuota(List<Long> productIds){
        return getUseQuota(productIds,null);
    }

    public Map<Long,BigDecimal> getUseQuota(List<Long> productIds,Long uid){
        if(CollectionUtils.isEmpty(productIds)){
            return new HashMap<>();
        }

        var query =
                new LambdaQueryWrapper<FinancialRecord>().in(FinancialRecord::getProductId, productIds);

        if(Objects.nonNull(uid)){
            query = query.eq(FinancialRecord :: getUid,uid);
        }

        var financialRecords = financialRecordMapper.selectList(query);
        return financialRecords.stream()
                // 按照 productId 分组
                .collect(Collectors.groupingBy(FinancialRecord :: getProductId))
                .entrySet().stream()
                // 将每组的金额相加
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<FinancialRecord> value = entry.getValue();
                            return value.stream().map(FinancialRecord :: getHoldAmount)
                                    .reduce(BigDecimal.ZERO,BigDecimal::add);
                        }
                ));
    }

    /**
     * 生成记录
     */
    public FinancialRecord generateFinancialRecord(Long uid,FinancialProduct product,BigDecimal amount){
        LocalDateTime startIncomeTime = TimeUtils.StartOfTime(TimeUtils.Util.DAY).plusDays(1);
        LocalDateTime startDate = requestInitService.now().plusDays(1L);
        FinancialRecord record = FinancialRecord.builder()
                .id(CommonFunction.generalId())
                .productId(product.getId())
                .riskType(product.getRiskType())
                .uid(uid).productType(product.getType())
                .holdAmount(amount)
                .purchaseTime(requestInitService.now())
                .productTerm(product.getTerm())
                .startIncomeTime(startIncomeTime)
                .endTime(startDate.plusDays(product.getTerm().getDay()))
                .rate(product.getRate())
                .coin(product.getCoin())
                .status(RecordStatus.PROCESS)
                .productName(product.getName())
                .productNameEn(product.getNameEn())
                .logo(product.getLogo())
                .build();
        int i = financialRecordMapper.insert(record);
        if(i <= 0){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return record;
    }

    /**
     * 根据产品类型、状态获取本金总额
     * @param uid uid
     * @param type 产品类型
     * @param status 产品状态
     */
    public BigDecimal getPurchaseAmount(Long uid, ProductType type, RecordStatus status){
        var financialPurchaseRecords = this.selectList(uid,type,status);
        return financialPurchaseRecords.stream().map(FinancialRecord::getHoldAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据产品类型、状态获取列表
     * @param uid uid
     * @param type 产品类型
     * @param status 产品状态
     */
    public List<FinancialRecord> selectList(Long uid, ProductType type, RecordStatus status){
        var query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getUid, uid)
                .eq(FinancialRecord::getStatus, status);

        if (Objects.nonNull(type)) {
            query = query.eq(FinancialRecord::getProductType, type);

        }
        return financialRecordMapper.selectList(query);
    }

    /**
     * 根据产品类型、状态获取列表
     * @param uid uid
     * @param type 产品类型
     * @param status 产品状态
     */
    public IPage<FinancialRecord> selectListPage(IPage<FinancialRecord> page,Long uid, ProductType type, RecordStatus status){
        var query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getUid, uid)
                .eq(FinancialRecord::getStatus, status);

        if (Objects.nonNull(type)) {
            query = query.eq(FinancialRecord::getProductType, type);

        }
        return financialRecordMapper.selectPage(page,query);
    }

    /**
     * 获取不同用户不同产品的汇总金额
     */
    public Map<Long,BigDecimal> getSummaryAmount(List<Long> uids,ProductType productType,RecordStatus recordStatus){
        var recordQuery = new LambdaQueryWrapper<FinancialRecord>()
                .in(FinancialRecord :: getUid,uids);

        if(Objects.nonNull(productType)){
            recordQuery= recordQuery.eq(FinancialRecord :: getProductType,productType);
        }
        if(Objects.nonNull(recordStatus)){
            recordQuery= recordQuery.eq(FinancialRecord :: getStatus,recordStatus);
        }


        Map<Long, List<FinancialRecord>> recordMapByUid = Optional.ofNullable(financialRecordMapper.selectList(recordQuery)).orElse(new ArrayList<>())
                .stream().collect(Collectors.groupingBy(FinancialRecord::getUid));
        EnumMap<CurrencyCoin, BigDecimal> dollarRateMap = currencyService.getDollarRateMap();

        return recordMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(record ->{
                    BigDecimal holdAmount = record.getHoldAmount();
                    BigDecimal rate = dollarRateMap.getOrDefault(record.getCoin(), BigDecimal.ZERO);
                    return holdAmount.multiply(rate);
                }).reduce(BigDecimal.ZERO,BigDecimal::add)
        ));
    }


    /**
     *  获取需要计算利息的分页记录信息
     */
    public IPage<FinancialRecord> needCalIncomeRecord(IPage<FinancialRecord> page){
        LambdaQueryWrapper<FinancialRecord> queryWrapper =
                new LambdaQueryWrapper<FinancialRecord>().eq(FinancialRecord::getStatus, RecordStatus.PROCESS);
        return financialRecordMapper.selectPage(page,queryWrapper);
    }
}