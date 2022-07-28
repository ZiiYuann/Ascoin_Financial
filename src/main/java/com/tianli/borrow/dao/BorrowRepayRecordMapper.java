package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowRepayRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.borrow.query.BorrowRepayQuery;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

/**
 * <p>
 * 借币还款记录 Mapper 接口
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Mapper
public interface BorrowRepayRecordMapper extends BaseMapper<BorrowRepayRecord> {
    BigDecimal selectRepaySumQuery(BorrowRepayQuery query);
}
