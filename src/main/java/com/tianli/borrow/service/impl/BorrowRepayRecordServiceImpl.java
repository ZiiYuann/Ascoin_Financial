package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.convert.BorrowOrderConverter;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.dao.BorrowRepayRecordMapper;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowRepayRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowRepayRecordVO;
import com.tianli.common.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

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
    @Autowired
    private BorrowOrderConverter borrowConverter;

    @Override
    public BigDecimal selectRepaySumQuery(BorrowRepayQuery query) {
        return borrowRepayRecordMapper.selectRepaySumQuery(query);
    }

    @Override
    public IPage<BorrowRepayRecordVO> repayRecordPage(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query) {
        LambdaQueryWrapper<BorrowRepayRecord> queryWrapper = new QueryWrapper<BorrowRepayRecord>().lambda();

        if(Objects.nonNull(query.getOrderId())){
            queryWrapper.eq(BorrowRepayRecord::getOrderId, query.getOrderId());
        }

        if(Objects.nonNull(query.getType())){
            queryWrapper.eq(BorrowRepayRecord::getType, query.getType());
        }

        if(Objects.nonNull(query.getStatus())){
            queryWrapper.eq(BorrowRepayRecord::getStatus, query.getStatus());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowRepayRecord::getRepayTime, query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowRepayRecord::getRepayTime, query.getEndTime());
        }

        queryWrapper.orderByDesc(BorrowRepayRecord::getRepayTime);
        return this.page(pageQuery.page(),queryWrapper).convert(borrowConverter::toRepayVO);
    }
}
