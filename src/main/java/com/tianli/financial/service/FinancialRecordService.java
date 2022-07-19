package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.TimeUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.FinancialRecordMapper;
import com.tianli.sso.init.RequestInitService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FinancialRecordService extends ServiceImpl<FinancialRecordMapper, FinancialRecord> {

    @Resource
    private FinancialRecordMapper financialRecordMapper;
    @Resource
    private RequestInitService requestInitService;

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
                .productName(product.getNameEn())
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
}
