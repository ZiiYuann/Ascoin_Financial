package com.tianli.borrow.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.convert.BorrowOrderConverter;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.dao.BorrowInterestRecordMapper;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.service.IBorrowInterestRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowInterestRecordVO;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import com.tianli.common.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    @Resource
    private BorrowOrderConverter borrowConverter;
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

    @Override
    public IPage<BorrowInterestRecordVO> interestRecordPage(PageQuery<BorrowInterestRecord> pageQuery, BorrowInterestRecordQuery query) {
        LambdaQueryWrapper<BorrowInterestRecord> queryWrapper = new QueryWrapper<BorrowInterestRecord>().lambda();

        if(Objects.nonNull(query.getOrderId())){
            queryWrapper.eq(BorrowInterestRecord::getOrderId,query.getOrderId());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowInterestRecord::getInterestAccrualTime,query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowInterestRecord::getInterestAccrualTime,query.getEndTime());
        }

        queryWrapper.orderByDesc(BorrowInterestRecord::getInterestAccrualTime);
        return this.page(pageQuery.page(),queryWrapper).convert(borrowConverter::toInterestVO);
    }
}
