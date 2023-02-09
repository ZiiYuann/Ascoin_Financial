package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.enums.BorrowStatus;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.BorrowConfigCoinIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.vo.MBorrowConfigCoinVO;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
public interface BorrowConfigCoinService extends IService<BorrowConfigCoin> {

    void insertOrUpdate(BorrowConfigCoinIoUQuery borrowConfigCoinIoUQuery);

    IPage<MBorrowConfigCoinVO> MBorrowConfigCoinVOPage(IPage<BorrowConfigCoin> page, BorrowQuery borrowQuery);

    void modifyStatus(String coin, BorrowStatus borrowStatus);

    void check(Long uid, BorrowCoinQuery query);
}
