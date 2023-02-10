package com.tianli.product.aborrow.service;

import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.vo.CalPledgeVO;

public interface BorrowService {

    void borrowCoin(Long uid, BorrowCoinQuery query);

    CalPledgeVO calPledge(Long uid, BorrowCoinQuery query);
}
