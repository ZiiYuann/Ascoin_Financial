package com.tianli.borrow.service.impl;

import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.dao.BorrowPledgeRecordMapper;
import com.tianli.borrow.service.IBorrowPledgeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 借币质押记录 服务实现类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Service
public class BorrowPledgeRecordServiceImpl extends ServiceImpl<BorrowPledgeRecordMapper, BorrowPledgeRecord> implements IBorrowPledgeRecordService {
    @Autowired
    private BorrowPledgeRecordMapper borrowPledgeRecordMapper;

    @Override
    public BigDecimal selectAmountSumByTime(Date startTime, Date endTime) {
        return borrowPledgeRecordMapper.selectAmountSumByTime(startTime,endTime);
    }

    @Override
    public List<BorrowOrderStatisticsChartVO> selectAmountChartByTime(Date startTime) {
        return borrowPledgeRecordMapper.selectAmountChartByTime(startTime);
    }
}
