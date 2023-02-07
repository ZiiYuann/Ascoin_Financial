package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.enums.BorrowStatus;
import com.tianli.product.aborrow.mapper.BorrowConfigPledgeMapper;
import com.tianli.product.aborrow.query.BorrowConfigPledgeIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.service.BorrowConfigPledgeService;
import com.tianli.product.aborrow.vo.MBorrowConfigPledgeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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


}
