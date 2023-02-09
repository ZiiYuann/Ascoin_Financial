package com.tianli.product.aborrow.service.impl;

import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowServiceImpl implements BorrowService {

    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowConfigCoinService borrowConfigCoinService;

    @Override
    @Transactional
    public void borrowCoin(Long uid, BorrowCoinQuery query) {

        borrowConfigCoinService.check(uid, query);

        borrowRecordCoinService.save(uid, query);
        query.getPledgeContext().forEach(context -> borrowRecordPledgeService.save(uid, context));

        borrowRecordService.calPledgeRate(uid, query.getAutoReplenishment());

    }
}
