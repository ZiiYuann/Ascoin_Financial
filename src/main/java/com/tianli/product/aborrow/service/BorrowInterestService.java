package com.tianli.product.aborrow.service;

import com.tianli.product.aborrow.entity.BorrowInterest;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
public interface BorrowInterestService {

    void add(Long bid, Long uid, String coin, BigDecimal amount);

    List<BorrowInterest> list(Long uid);

    BorrowInterest get(Long uid, Long bid, String coin);

    void reduce(Long bid, Long uid, String coin, BigDecimal amount);

    boolean payOff(Long uid,Long bid);
}
