package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.enums.BorrowStatus;
import com.tianli.product.aborrow.mapper.BorrowConfigCoinMapper;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.BorrowConfigCoinIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.service.BorrowConfigCoinService;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.vo.MBorrowConfigCoinVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Service
public class BorrowConfigCoinServiceImpl extends ServiceImpl<BorrowConfigCoinMapper, BorrowConfigCoin>
        implements BorrowConfigCoinService {

    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;

    @Override
    @Transactional
    public void insertOrUpdate(BorrowConfigCoinIoUQuery query) {
        boolean contains = coinBaseService.pushCoinNames().contains(query.getCoin());
        if (!contains) {
            throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
        }
        BorrowConfigCoin borrowConfigCoin = borrowConvert.toBorrowConfigCoin(query);
        this.saveOrUpdate(borrowConfigCoin, new LambdaQueryWrapper<BorrowConfigCoin>()
                .eq(BorrowConfigCoin::getCoin, query.getCoin()));
    }

    @Override
    public IPage<MBorrowConfigCoinVO> MBorrowConfigCoinVOPage(IPage<BorrowConfigCoin> page, BorrowQuery borrowQuery) {
        return this.page(page, QueryWrapperUtils.generate(BorrowConfigCoin.class, borrowQuery))
                .convert(record -> borrowConvert.toMBorrowConfigCoinVO(record));
    }

    @Override
    @Transactional
    public void modifyStatus(String coin, BorrowStatus borrowStatus) {
        BorrowQuery query = BorrowQuery.builder()
                .coin(coin).build();
        BorrowConfigCoin borrowConfigCoin = this.getById(coin);
        borrowConfigCoin.setStatus(borrowStatus.getStatus());
        this.update(borrowConfigCoin, QueryWrapperUtils.generate(BorrowConfigCoin.class, query));
    }

    @Override
    public void check(Long uid, BorrowCoinQuery query) {
        BorrowConfigCoin borrowConfigCoin = this.getById(query.getBorrowCoin());

        if (borrowConfigCoin.getStatus() != 1) {
            throw ErrorCodeEnum.BORROW_COIN_NOT_OPEN.generalException();
        }

        BorrowRecordCoin borrowRecordCoin = borrowRecordCoinService.getOne(uid, query.getBorrowCoin());
        BigDecimal totalBorrowAmount = borrowRecordCoin.getAmount().add(query.getBorrowAmount());
        if (totalBorrowAmount.compareTo(borrowConfigCoin.getMinAmount()) < 0) {
            throw ErrorCodeEnum.BORROW_AMOUNT_MIN_ERROR.generalException();
        }

        if (Objects.nonNull(borrowConfigCoin.getMaxAmount()) && totalBorrowAmount.compareTo(borrowConfigCoin.getMaxAmount()) > 0) {
            throw ErrorCodeEnum.BORROW_AMOUNT_MAX_ERROR.generalException();
        }
    }

}
