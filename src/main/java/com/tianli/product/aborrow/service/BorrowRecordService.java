package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.query.BorrowCoinQuery;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordService extends IService<BorrowRecord> {

    void calPledgeRate(Long uid, Boolean autoReplenishment);

    PledgeRateDto preCalPledgeRate(Long uid, BorrowCoinQuery borrowCoinQuery);

    PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos
            , List<BorrowRecordCoin> borrowRecordCoins);


}
