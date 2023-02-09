package com.tianli.product.aborrow.service;

import com.tianli.product.aborrow.query.BorrowCoinQuery;

public interface BorrowService {

    void borrowCoin(Long uid, BorrowCoinQuery query);

}
