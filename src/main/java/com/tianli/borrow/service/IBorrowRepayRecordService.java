package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.vo.BorrowRepayRecordVO;
import com.tianli.common.PageQuery;

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

    IPage<BorrowRepayRecordVO> repayRecordPage(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query);
}
