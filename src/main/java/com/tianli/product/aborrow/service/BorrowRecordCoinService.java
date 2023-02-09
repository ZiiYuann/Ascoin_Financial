package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.query.BorrowCoinQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordCoinService extends IService<BorrowRecordCoin> {

    void save(Long uid, BorrowCoinQuery query);

    List<BorrowRecordCoin> listByUid(Long uid);

    BorrowRecordCoin getOne(Long uid, String coin);
}
