package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.query.BorrowHedgeEntrustIoUQuery;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.entity.BorrowHedgeEntrust;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.enums.HedgeStatus;
import com.tianli.product.aborrow.enums.HedgeType;
import com.tianli.product.aborrow.mapper.BorrowHedgeEntrustMapper;
import com.tianli.product.aborrow.query.MBorrowHedgeQuery;
import com.tianli.product.aborrow.service.BorrowHedgeEntrustService;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.vo.MBorrowConfigPledgeVO;
import com.tianli.product.aborrow.vo.MBorrowHedgeEntrustVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class BorrowHedgeEntrustServiceImpl extends ServiceImpl<BorrowHedgeEntrustMapper, BorrowHedgeEntrust>
        implements BorrowHedgeEntrustService {

    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private CurrencyService currencyService;

    @Override
    public void manual(BorrowHedgeEntrustIoUQuery query) {
        Long brId = query.getBrId();
        BorrowRecordCoin borrowRecordCoin = borrowRecordCoinService.getById(brId);

        // 市价

        BorrowHedgeEntrust borrowHedgeEntrust = BorrowHedgeEntrust.builder()
                .bid(borrowRecordCoin.getBid())
                .brId(brId)
                .createRate(currencyService.getDollarRate(borrowRecordCoin.getCoin()))
                .coin(borrowRecordCoin.getCoin())
                .hedgeCoin(query.getHedgeCoin())
                .amount(borrowRecordCoin.getAmount())
                .entrustRate(query.getEntrustRate())
                .hedgeType(HedgeType.MANUAL)
                .hedgeStatus(HedgeStatus.PROCESS)
                .build();
        baseMapper.insert(borrowHedgeEntrust);
    }

    @Override
    public IPage<MBorrowHedgeEntrustVO> vos(Page<BorrowHedgeEntrust> page, MBorrowHedgeQuery query) {
        return this.page(page, QueryWrapperUtils.generate(BorrowHedgeEntrust.class, query))
                .convert(index -> borrowConvert.toMBorrowHedgeEntrustVO(index));
    }


}
