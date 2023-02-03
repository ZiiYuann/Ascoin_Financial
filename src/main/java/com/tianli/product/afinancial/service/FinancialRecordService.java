package com.tianli.product.afinancial.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.common.CommonFunction;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.ProductSummaryDataDto;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.RecordStatus;
import com.tianli.product.afinancial.mapper.FinancialRecordMapper;
import com.tianli.product.afinancial.query.FinancialRecordQuery;
import com.tianli.product.afinancial.query.RecordRenewalQuery;
import com.tianli.product.service.FinancialProductService;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    @Resource
    private FinancialProductService financialProductService;

    /**
     * 赎回金额
     */
    @Transactional
    public void redeem(Long recordId, BigDecimal redeemAmount, BigDecimal originalHoldAmount) {

        FinancialRecord record = financialRecordMapper.selectById(recordId);

        // 减少产品额度
        financialProductService.reduceUseQuota(record.getProductId(), redeemAmount);

        // 如果存在待记利息金额，优先扣除
        if (record.getWaitAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal reduceIncomeAmount = BigDecimal.ZERO;
            BigDecimal reduceWaitAmount;

            if (record.getWaitAmount().compareTo(redeemAmount) > 0) {
                reduceWaitAmount = redeemAmount;
            } else {
                reduceWaitAmount = record.getWaitAmount();
                reduceIncomeAmount = redeemAmount.subtract(reduceWaitAmount);
            }

            if (financialRecordMapper.reduce2(recordId, reduceIncomeAmount, reduceWaitAmount, originalHoldAmount) < 0) {
                log.error("赎回异常，recordId:{},amount:{}", recordId, redeemAmount);
                ErrorCodeEnum.throwException("用户申购记录金额发生变化，请重试");
            }
            return;
        }

        if (financialRecordMapper.reduce(recordId, redeemAmount, originalHoldAmount) < 0) {
            log.error("赎回异常，recordId:{},amount:{}", recordId, redeemAmount);
            ErrorCodeEnum.throwException("用户申购记录金额发生变化，请重试");
        }
    }

    /**
     * 默认通过id查询需要带上uid
     */
    public FinancialRecord selectById(Long recordId, Long uid) {
        if (Objects.isNull(recordId) || Objects.isNull(uid)) {
            ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
        }

        LambdaQueryWrapper<FinancialRecord> query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getId, recordId).eq(FinancialRecord::getUid, uid);

        return Optional.ofNullable(financialRecordMapper.selectOne(query)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
    }

    public List<FinancialRecord> selectByProductId(Long productId) {
        if (Objects.isNull(productId)) {
            ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
        }

        LambdaQueryWrapper<FinancialRecord> query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getProductId, productId);

        return Optional.ofNullable(financialRecordMapper.selectList(query)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
    }

    public List<FinancialRecord> selectByProductId(Long productId, Long uid) {
        if (Objects.isNull(productId)) {
            ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
        }

        LambdaQueryWrapper<FinancialRecord> query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getProductId, productId)
                .eq(FinancialRecord::getUid, uid);

        return Optional.ofNullable(financialRecordMapper.selectList(query)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
    }

    /**
     * 每个产品第一个有效持有记录id long,long   productId,recordId
     */
    public Map<Long, Long> firstProcessRecordMap(List<Long> productIds, Long uid) {
        if (CollectionUtils.isEmpty(productIds)) {
            return new HashMap<>();
        }
        final Map<Long, Long> result = new HashMap<>();
        financialRecordMapper.firstProcessRecordMap(productIds, uid).forEach(index -> result.put((Long) index.get("product_id"), (Long) index.get("record_id")));
        return result;
    }

    public Boolean isNewUser(Long uid) {
        FinancialRecord record = financialRecordMapper
                .selectOne(new LambdaQueryWrapper<FinancialRecord>().eq(FinancialRecord::getUid, uid).last("limit 1"));
        return Objects.isNull(record);
    }

    public Map<Long, BigDecimal> getUseQuota(List<Long> productIds, Long uid) {
        if (CollectionUtils.isEmpty(productIds)) {
            return new HashMap<>();
        }

        var query =
                new LambdaQueryWrapper<FinancialRecord>()
                        .eq(FinancialRecord::getStatus, RecordStatus.PROCESS)
                        .in(FinancialRecord::getProductId, productIds);

        if (Objects.nonNull(uid)) {
            query = query.eq(FinancialRecord::getUid, uid);
        }

        var financialRecords = financialRecordMapper.selectList(query);
        return financialRecords.stream()
                // 按照 productId 分组
                .collect(Collectors.groupingBy(FinancialRecord::getProductId))
                .entrySet().stream()
                // 将每组的金额相加
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<FinancialRecord> value = entry.getValue();
                            return value.stream().map(FinancialRecord::getHoldAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                        }
                ));
    }

    /**
     * 生成记录
     */
    public FinancialRecord generateFinancialRecord(Long uid, FinancialProduct product, BigDecimal amount, boolean autoRenewal) {
        return generateFinancialRecord(null, uid, product, amount, autoRenewal);
    }

    public FinancialRecord generateFinancialRecord(Long id, Long uid, FinancialProduct product, BigDecimal amount, boolean autoRenewal) {
        LocalDateTime startIncomeTime = DateUtil.beginOfDay(new Date()).toLocalDateTime().plusDays(1);
        FinancialRecord record = FinancialRecord.builder()
                .id(MoreObjects.firstNonNull(id, CommonFunction.generalId()))
                .productId(product.getId())
                .riskType(product.getRiskType())
                .businessType(product.getBusinessType())
                .uid(uid).productType(product.getType())
                .holdAmount(amount)
                .purchaseTime(requestInitService.now())
                .productTerm(product.getTerm())
                .startIncomeTime(startIncomeTime)
                .endTime(startIncomeTime.plusDays(product.getTerm().getDay()))
                .rate(product.getRate())
                .coin(product.getCoin())
                .status(RecordStatus.PROCESS)
                .productName(product.getName())
                .productNameEn(product.getNameEn())
                .logo(product.getLogo())
                .autoRenewal(autoRenewal)
                .waitAmount(amount)
                .build();
        int i = financialRecordMapper.insert(record);
        if (i <= 0) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return record;
    }

    /**
     * 根据产品类型、状态获取本金总额
     *
     * @param uid    uid
     * @param type   产品类型
     * @param status 产品状态
     */
    public BigDecimal getPurchaseAmount(Long uid, ProductType type, RecordStatus status) {
        var financialPurchaseRecords = this.selectList(uid, type, status);
        return financialPurchaseRecords.stream()
                .map(o -> o.getHoldAmount().multiply(currencyService.getDollarRate(o.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据产品类型、状态获取列表
     *
     * @param uid    uid
     * @param type   产品类型
     * @param status 产品状态
     */
    public List<FinancialRecord> selectList(Long uid, ProductType type, RecordStatus status) {
        var query = new LambdaQueryWrapper<FinancialRecord>()
                .eq(FinancialRecord::getUid, uid)
                .eq(FinancialRecord::getStatus, status);

        if (Objects.nonNull(type)) {
            query = query.eq(FinancialRecord::getProductType, type);

        }
        return financialRecordMapper.selectList(query);
    }

    /**
     * 获取不同用户不同产品的汇总金额
     */
    public Map<Long, BigDecimal> getSummaryAmount(List<Long> uids, ProductType productType, RecordStatus recordStatus) {
        var recordQuery = new LambdaQueryWrapper<FinancialRecord>()
                .in(FinancialRecord::getUid, uids);

        if (Objects.nonNull(productType)) {
            recordQuery = recordQuery.eq(FinancialRecord::getProductType, productType);
        }
        if (Objects.nonNull(recordStatus)) {
            recordQuery = recordQuery.eq(FinancialRecord::getStatus, recordStatus);
        }


        Map<Long, List<FinancialRecord>> recordMapByUid = Optional.ofNullable(financialRecordMapper.selectList(recordQuery)).orElse(new ArrayList<>())
                .stream().collect(Collectors.groupingBy(FinancialRecord::getUid));

        return recordMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(record -> {
                    BigDecimal holdAmount = record.getHoldAmount();
                    BigDecimal rate = currencyService.getDollarRate(record.getCoin());
                    return holdAmount.multiply(rate);
                }).reduce(BigDecimal.ZERO, BigDecimal::add)
        ));
    }

    /**
     * 获取需要计算利息的分页记录信息
     */
    public IPage<FinancialRecord> needCalIncomeRecord(IPage<FinancialRecord> page) {
        LambdaQueryWrapper<FinancialRecord> queryWrapper =
                new LambdaQueryWrapper<FinancialRecord>().eq(FinancialRecord::getStatus, RecordStatus.PROCESS);
        return financialRecordMapper.selectPage(page, queryWrapper);
    }

    /**
     * 正持有的产品数量
     */
    public BigDecimal holdSingleCoin(FinancialRecordQuery query) {
        List<AmountDto> amountDtos = financialRecordMapper.holdAmount(query);
        return amountDtos.stream().map(AmountDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal dollarHold(FinancialRecordQuery query) {
        List<AmountDto> amountDtos = financialRecordMapper.holdAmount(query);
        return currencyService.calDollarAmount(amountDtos);
    }

    public List<AmountDto> hold(FinancialRecordQuery query) {
        return financialRecordMapper.holdAmount(query);
    }

    /**
     * 正持有的产品的用户数量
     */
    public BigInteger countUid() {
        return Optional.ofNullable(financialRecordMapper.countUid()).orElse(BigInteger.ZERO);
    }

    /**
     * 配置是否自动续费
     */
    @Transactional
    public void recordRenewal(RecordRenewalQuery query) {
        Long uid = requestInitService.uid();
        FinancialRecord record = this.selectById(query.getRecordId(), uid);

        if (!RecordStatus.PROCESS.equals(record.getStatus())) {
            log.info("当前产品状态不为持有,recordId:{}", query.getRecordId());
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException("当前产品状态不为持有");
        }

        record.setUpdateTime(LocalDateTime.now());
        record.setAutoRenewal(query.isAutoRenewal());
        financialRecordMapper.updateById(record);
    }

    /**
     * 获取产品相关的汇总信息
     */
    @SuppressWarnings("unchecked")
    public Map<Long, ProductSummaryDataDto> getProductSummaryDataDtoMap(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return MapUtils.EMPTY_MAP;
        }
        return Optional.ofNullable(financialRecordMapper.listProductSummaryDataDto(productIds))
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.toMap(ProductSummaryDataDto::getProductId, o -> o));
    }

    /**
     * 增加待记利息金额
     */
    public void increaseWaitAmount(Long recordId, BigDecimal amount, BigDecimal originalAmount) {
        int i = financialRecordMapper.increaseWaitAmount(recordId, amount, originalAmount);
        if (i <= 0) {
            ErrorCodeEnum.throwException("待金额金额发生变动，请重试");
        }
    }

    /**
     * 增加记录利息
     */
    public void increaseIncomeAmount(Long recordId, BigDecimal amount, BigDecimal originalAmount) {
        int i = financialRecordMapper.increaseIncomeAmount(recordId, amount, originalAmount);
        if (i <= 0) {
            ErrorCodeEnum.throwException("记录金额金额发生变动，请重试");
        }
    }

    /**
     * 修改record的年化利率
     */
    public void updateRateByProductId(Long productId, BigDecimal rate) {
        int hour = LocalDateTime.now().getHour();
        if (hour <= 2) {
            ErrorCodeEnum.throwException("计算利息时间段不允许修改产品年华利率");
        }
        financialRecordMapper.updateRateByProductId(productId, rate);
    }

}
