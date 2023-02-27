package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.enums.PledgeStatus;
import com.tianli.product.aborrow.query.BorrowUserQuery;
import com.tianli.product.aborrow.vo.MBorrowUserVO;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordService extends IService<BorrowRecord> {

    BorrowRecord copy(Long bid, PledgeStatus pledgeStatus);

    BorrowRecord getAndInit(Long uid, Boolean autoReplenishment);

    BorrowRecord getValid(Long uid);

    BorrowRecord get(Long uid);

    IPage<MBorrowUserVO> pledgeUsers(Page<BorrowRecord> page, BorrowUserQuery query);

    void finish(Long uid, Long bid);
}
