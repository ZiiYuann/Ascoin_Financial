package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import com.tianli.product.aborrow.vo.BorrowRecordCoinVO;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordCoinService extends IService<BorrowRecordCoin> {

    void save(Long uid, Long bid, BorrowCoinQuery query);

    List<BorrowRecordCoin> listByUid(Long uid, Long bid);

    List<BorrowRecordCoinVO> vos(Long uid, Long bid);

    BorrowRecordCoin getOne(Long uid, String coin);

    void repay(Long uid, Long bid, RepayCoinQuery repayCoinQuery);

    boolean payOff(Long uid, Long bid);

    void calInterest(Long uid, Long bid);

}
