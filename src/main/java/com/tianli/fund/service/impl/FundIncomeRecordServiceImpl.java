package com.tianli.fund.service.impl;

import com.tianli.fund.dao.FundIncomeRecordMapper;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.dto.AmountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 基金收益记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundIncomeRecordServiceImpl extends ServiceImpl<FundIncomeRecordMapper, FundIncomeRecord> implements IFundIncomeRecordService {

    @Autowired
    private FundIncomeRecordMapper fundIncomeRecordMapper;

    @Override
    public List<AmountDto> amountSumByUid(Long uid, Integer status) {
        return fundIncomeRecordMapper.amountSumByUid(uid,status);
    }
}
