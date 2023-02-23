package com.tianli.product.aborrow.service;

import com.tianli.product.aborrow.entity.BorrowInterest;
import com.tianli.product.aborrow.enums.InterestType;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
public interface BorrowInterestService {

    void add(Long bid, Long uid, String coin, BigDecimal amount);

    void add(Long bid, Long uid, String coin, BigDecimal amount, InterestType interestType);

    List<BorrowInterest> list(Long uid,Long bid);

    BorrowInterest get(Long uid, Long bid, String coin);

    void reduce(Long uid, Long bid, String coin, BigDecimal amount);

    void reduceAll(Long uid, Long bid, String coin);

    boolean payOff(Long uid, Long bid);

}
