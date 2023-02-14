package com.tianli.product.aborrow.service;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
public interface BorrowInterestService {

    void add(Long bid, Long uid, String coin, BigDecimal amount);

}
