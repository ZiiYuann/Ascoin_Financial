package com.tianli.borrow.service.impl;

import cn.hutool.core.date.DateTime;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.dao.BorrowInterestRecordMapper;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.service.IBorrowInterestRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 借币利息记录 服务实现类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Service
public class BorrowInterestRecordServiceImpl extends ServiceImpl<BorrowInterestRecordMapper, BorrowInterestRecord> implements IBorrowInterestRecordService {

    @Autowired
    private BorrowInterestRecordMapper borrowInterestRecordMapper;
    @Override
    public BigDecimal selectInterestSumByQuery(BorrowInterestRecordQuery query) {
        return borrowInterestRecordMapper.selectInterestSumByQuery(query);
    }

    @Override
    public List<BorrowOrderStatisticsChartVO> selectInterestChartByTime(DateTime beginOfDay) {
        return borrowInterestRecordMapper.selectInterestChartByTime(beginOfDay);
    }
}
