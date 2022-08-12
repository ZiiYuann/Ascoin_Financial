package com.tianli.borrow.service;

import com.tianli.borrow.entity.BorrowRepayRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.query.BorrowRepayQuery;

import java.math.BigDecimal;

/**
 * <p>
 * 借币还款记录 服务类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
public interface IBorrowRepayRecordService extends IService<BorrowRepayRecord> {

    BigDecimal selectRepaySumQuery(BorrowRepayQuery query);
}
