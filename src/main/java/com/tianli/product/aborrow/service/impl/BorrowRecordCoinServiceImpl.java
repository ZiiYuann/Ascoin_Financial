package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.mapper.BorrowRecordCoinMapper;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowRecordCoinServiceImpl extends ServiceImpl<BorrowRecordCoinMapper, BorrowRecordCoin>
        implements BorrowRecordCoinService {

    @Resource
    private CurrencyService currencyService;

    @Override
    @Transactional
    public void save(Long uid, BorrowCoinQuery query) {
        String coin = query.getBorrowCoin();
        BorrowRecordCoin borrowRecordCoin = getAndInit(uid, coin);
        this.casIncrease(uid, coin, query.getBorrowAmount(), borrowRecordCoin.getAmount());
        // todo 记录日志
    }

    @Override
    public List<BorrowRecordCoin> listByUid(Long uid) {
        return this.list(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid));
    }

    @Override
    public BorrowRecordCoin getOne(Long uid, String coin) {
        return this.getOne(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getCoin, coin));
    }

    private void casIncrease(Long uid, String coin, BigDecimal increaseAmount, BigDecimal originalAmount) {
        int i = baseMapper.casIncrease(uid, coin, increaseAmount, originalAmount);
        if (i == 1) {
            throw ErrorCodeEnum.BORROW_RECORD_COIN_ERROR.generalException();
        }
    }

    private void casDecrease(Long uid, String coin, BigDecimal decreaseAmount, BigDecimal originalAmount) {
        int i = baseMapper.casDecrease(uid, coin, decreaseAmount, originalAmount);
        if (i == 1) {
            throw ErrorCodeEnum.BORROW_RECORD_COIN_ERROR.generalException();
        }
    }

    private BorrowRecordCoin getAndInit(Long uid, String coin) {
        BorrowRecordCoin borrowRecordCoin = this.getOne(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getCoin, coin));

        if (Objects.isNull(borrowRecordCoin)) {
            borrowRecordCoin = BorrowRecordCoin.builder()
                    .uid(uid)
                    .coin(coin)
                    .amount(BigDecimal.ZERO)
                    .build();
            this.save(borrowRecordCoin);
        }
        return borrowRecordCoin;
    }
}
