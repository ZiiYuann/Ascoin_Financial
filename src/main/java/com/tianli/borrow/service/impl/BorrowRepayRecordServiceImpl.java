package com.tianli.borrow.service.impl;

import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.dao.BorrowRepayRecordMapper;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowRepayRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 借币还款记录 服务实现类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Service
public class BorrowRepayRecordServiceImpl extends ServiceImpl<BorrowRepayRecordMapper, BorrowRepayRecord> implements IBorrowRepayRecordService {
    @Autowired
    private BorrowRepayRecordMapper borrowRepayRecordMapper;
    @Override
    public BigDecimal selectRepaySumQuery(BorrowRepayQuery query) {
        return borrowRepayRecordMapper.selectRepaySumQuery(query);
    }
}
