package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.enums.BorrowStatus;
import com.tianli.product.aborrow.query.BorrowConfigPledgeIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.vo.BorrowConfigPledgeVO;
import com.tianli.product.aborrow.vo.MBorrowConfigCoinVO;
import com.tianli.product.aborrow.vo.MBorrowConfigPledgeVO;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
public interface BorrowConfigPledgeService extends IService<BorrowConfigPledge> {

    void insertOrUpdate(BorrowConfigPledgeIoUQuery borrowConfigPledgeIoUQuery);

    IPage<MBorrowConfigPledgeVO> MBorrowConfigCoinVOPage(IPage<BorrowConfigPledge> page, BorrowQuery borrowQuery);

    void modifyStatus(String coin, BorrowStatus borrowStatus);

    List<BorrowConfigPledgeVO> getVOs();
}

