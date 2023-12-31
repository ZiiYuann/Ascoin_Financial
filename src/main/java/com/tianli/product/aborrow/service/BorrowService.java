package com.tianli.product.aborrow.service;

import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.BorrowInterest;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.CalPledgeQuery;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import com.tianli.product.aborrow.vo.BorrowRecordSnapshotVO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface BorrowService {

    void borrowCoin(Long uid, BorrowCoinQuery query);

    void repayCoin(Long uid, RepayCoinQuery query);

    void modifyPledgeContext(Long uid, ModifyPledgeContextQuery query);

    BorrowRecord calPledgeRate(BorrowRecord borrowRecord, Long uid, Boolean autoReplenishment, boolean borrow);

    PledgeRateDto preCalPledgeRate(Long uid, CalPledgeQuery borrowCoinQuery, boolean borrowOperation);

    PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos
            , List<BorrowRecordCoin> borrowRecordCoins
            , List<BorrowInterest> borrowInterests
            , boolean borrow);

    PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos
            , List<BorrowRecordCoin> borrowRecordCoins
            , List<BorrowInterest> borrowInterests
            , boolean borrow, BigDecimal rate);

    BorrowRecordSnapshotVO newestSnapshot(Long uid);

    /**
     * 自动补仓
     */
    void autoReplenishment(BorrowRecord borrowRecord);

    /**
     * 强制平仓
     */
    void reduce(BorrowRecord borrowRecord);

    /**
     * 强制平仓 + 自动清算
     */
    void liquidate(BorrowRecord borrowRecord);

}
