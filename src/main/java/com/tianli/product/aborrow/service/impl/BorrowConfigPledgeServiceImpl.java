package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.enums.BorrowStatus;
import com.tianli.product.aborrow.mapper.BorrowConfigPledgeMapper;
import com.tianli.product.aborrow.query.BorrowConfigPledgeIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.service.BorrowConfigPledgeService;
import com.tianli.product.aborrow.vo.*;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.RecordStatus;
import com.tianli.product.afinancial.service.FinancialRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Service
public class BorrowConfigPledgeServiceImpl extends ServiceImpl<BorrowConfigPledgeMapper, BorrowConfigPledge>
        implements BorrowConfigPledgeService {

    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private FinancialRecordService financialRecordService;

    @Override
    public void insertOrUpdate(BorrowConfigPledgeIoUQuery query) {
        boolean contains = coinBaseService.pushCoinNames().contains(query.getCoin());
        if (!contains) {
            throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
        }
        var borrowConfigPledge = borrowConvert.toBorrowConfigPledge(query);
        this.saveOrUpdate(borrowConfigPledge, new LambdaQueryWrapper<BorrowConfigPledge>()
                .eq(BorrowConfigPledge::getCoin, query.getCoin()));
    }

    @Override
    public IPage<MBorrowConfigPledgeVO> MBorrowConfigCoinVOPage(IPage<BorrowConfigPledge> page, BorrowQuery borrowQuery) {
        return this.page(page, QueryWrapperUtils.generate(BorrowConfigPledge.class, borrowQuery))
                .convert(record -> borrowConvert.toMBorrowConfigPledgeVO(record));
    }

    @Override
    @Transactional
    public void modifyStatus(String coin, BorrowStatus borrowStatus) {
        BorrowQuery query = BorrowQuery.builder()
                .coin(coin).build();
        BorrowConfigPledge borrowConfigPledge = this.getById(coin);
        borrowConfigPledge.setStatus(borrowStatus.getStatus());
        this.update(borrowConfigPledge, QueryWrapperUtils.generate(BorrowConfigPledge.class, query));
    }

    @Override
    public List<BorrowConfigPledgeVO> getVOs() {
        return this.list(new LambdaQueryWrapper<BorrowConfigPledge>()
                        .eq(BorrowConfigPledge::getStatus, 1))
                .stream()
                .map(borrowConvert::toBorrowConfigPledgeVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountPledgeVO> getAccountPledgeVOs(Long uid) {
        List<BorrowConfigPledgeVO> vOs = this.getVOs();
        return vOs.stream().map(coin -> {
            AccountBalance accountBalance = accountBalanceService.getAndInit(uid, coin.getCoin());
            CoinBase coinBase = coinBaseService.getByName(coin.getCoin());
            return AccountPledgeVO.builder()
                    .coin(coin.getCoin())
                    .remain(accountBalance.getRemain())
                    .rate(currencyService.getDollarRate(coin.getCoin()))
                    .logo(coinBase.getLogo()).build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductPledgeVO> getProductPledgeVOs(Long uid) {
        List<BorrowConfigPledgeVO> vOs = this.getVOs();
        List<String> coins = vOs.stream().map(BorrowConfigPledgeVO::getCoin).collect(Collectors.toList());
        return financialRecordService.list(new LambdaQueryWrapper<FinancialRecord>()
                        .in(FinancialRecord::getCoin, coins)
                        .eq(FinancialRecord::getStatus, RecordStatus.PROCESS)
                        .eq(FinancialRecord::getUid, uid)
                        .eq(FinancialRecord::getProductType, ProductType.current)
                        .eq(FinancialRecord::isPledge, false)
                )
                .stream().map(financialRecord -> borrowConvert.toProductPledgeVO(financialRecord))
                .collect(Collectors.toList());

    }

    @Override
    public BorrowConfigPledge getById(String coin) {
        BorrowConfigPledge borrowConfigPledge = baseMapper.selectOne(new LambdaQueryWrapper<BorrowConfigPledge>()
                .eq(BorrowConfigPledge::getStatus, 1)
                .eq(BorrowConfigPledge::getCoin, coin));
        return Optional.ofNullable(borrowConfigPledge).orElseThrow(ErrorCodeEnum.BORROW_CONFIG_PLEDGE_NOT_OPEN::generalException);
    }


}
