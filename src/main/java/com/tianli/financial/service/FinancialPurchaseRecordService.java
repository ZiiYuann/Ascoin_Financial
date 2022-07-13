package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.financial.entity.FinancialPurchaseRecord;
import com.tianli.financial.enums.FinancialLogStatus;
import com.tianli.financial.enums.FinancialProductType;
import com.tianli.financial.mapper.FinancialPurchaseRecordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialPurchaseRecordService extends ServiceImpl<FinancialPurchaseRecordMapper, FinancialPurchaseRecord> {

    @Resource
    private FinancialPurchaseRecordMapper userFinancialPurchaseRecordMapper;

    /**
     * 根据产品类型、状态获取本金总额
     * @param uid uid
     * @param type 产品类型
     * @param status 产品状态
     */
    public BigDecimal getPurchaseAmount(Long uid, FinancialProductType type, FinancialLogStatus status){
        List<FinancialPurchaseRecord> financialPurchaseRecords = userFinancialPurchaseRecordMapper.selectList(new LambdaQueryWrapper<FinancialPurchaseRecord>()
                .eq(FinancialPurchaseRecord::getUid, uid)
                .eq(FinancialPurchaseRecord::getFinancialProductType, type)
                .eq(FinancialPurchaseRecord::getStatus, status.getType()));
        return financialPurchaseRecords.stream().map(FinancialPurchaseRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
