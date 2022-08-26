package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.contant.BorrowPledgeType;
import com.tianli.borrow.convert.BorrowOrderConverter;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.dao.BorrowPledgeRecordMapper;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.service.IBorrowPledgeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import com.tianli.borrow.vo.BorrowPledgeRecordVO;
import com.tianli.common.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private BorrowOrderConverter borrowConverter;

    @Override
    public BigDecimal selectAmountSumByTime(Date startTime, Date endTime) {
        return borrowPledgeRecordMapper.selectAmountSumByTime(startTime,endTime);
    }

    @Override
    public List<BorrowOrderStatisticsChartVO> selectAmountChartByTime(Date startTime) {
        return borrowPledgeRecordMapper.selectAmountChartByTime(startTime);
    }

    @Override
    public IPage<BorrowPledgeRecordVO> pledgeRecordPage(PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query) {
        LambdaQueryWrapper<BorrowPledgeRecord> queryWrapper = new QueryWrapper<BorrowPledgeRecord>().lambda();

        if(Objects.nonNull(query.getOrderId())){
            queryWrapper.eq(BorrowPledgeRecord::getOrderId,query.getOrderId());
        }

        if(Objects.nonNull(query.getType())){
            queryWrapper.eq(BorrowPledgeRecord::getType,query.getType());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowPledgeRecord::getPledgeTime,query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowPledgeRecord::getPledgeTime,query.getEndTime());
        }

        queryWrapper.orderByDesc(BorrowPledgeRecord::getPledgeTime);

        return this.page(pageQuery.page(), queryWrapper).convert(borrowConverter::toPledgeVO);
    }
}
