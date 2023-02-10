package com.tianli.product.aborrow.service;

import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.CalPledgeQuery;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface BorrowService {

    void borrowCoin(Long uid, BorrowCoinQuery query);

    void calPledgeRate(Long uid, Boolean autoReplenishment);

    PledgeRateDto preCalPledgeRate(Long uid, CalPledgeQuery borrowCoinQuery);

    PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos
            , List<BorrowRecordCoin> borrowRecordCoins);
}
