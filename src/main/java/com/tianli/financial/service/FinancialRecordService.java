package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.mapper.FinancialRecordMapper;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class FinancialRecordService extends ServiceImpl<FinancialRecordMapper, FinancialRecord> {

    @Resource
    private FinancialRecordMapper financialRecordMapper;
    @Resource
    private RequestInitService requestInitService;

    /**
     * 生成记录
     */
    public FinancialRecord generateFinancialRecord(Long uid,FinancialProduct product,BigDecimal amount){
        LocalDate startDate = requestInitService.now().toLocalDate().plusDays(1L);
        FinancialRecord record = FinancialRecord.builder()
                .id(CommonFunction.generalId())
                .productId(product.getId())
                .uid(uid).financialProductType(product.getType())
                .amount(amount)
                .createTime(requestInitService.now())
                .startDate(startDate)
                .endDate(startDate.plusDays(product.getTerm().getDay()))
                .rate(product.getRate())
                .status(RecordStatus.PROCESS)
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
        return financialPurchaseRecords.stream().map(FinancialRecord::getAmount)
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
            query = query.eq(FinancialRecord::getFinancialProductType, type);

        }
        return financialRecordMapper.selectList(query);
    }
}
