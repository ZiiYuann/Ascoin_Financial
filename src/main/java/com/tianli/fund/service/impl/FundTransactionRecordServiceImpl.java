package com.tianli.fund.service.impl;

import com.tianli.fund.contant.FundTransactionStatus;
import com.tianli.fund.dao.FundTransactionRecordMapper;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.enums.FundTransactionType;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * <p>
 * 基金交易记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundTransactionRecordServiceImpl extends ServiceImpl<FundTransactionRecordMapper, FundTransactionRecord> implements IFundTransactionRecordService {

    @Autowired
    private FundTransactionRecordMapper fundTransactionRecordMapper;

    @Override
    public BigDecimal getWaitRedemptionAmount(Long fundId) {

        return fundTransactionRecordMapper.TransactionAmountSum(fundId, FundTransactionType.redemption, FundTransactionStatus.wait_audit);
    }
}
