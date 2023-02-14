package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowInterest;
import com.tianli.product.aborrow.entity.BorrowInterestLog;
import com.tianli.product.aborrow.mapper.BorrowInterestLogMapper;
import com.tianli.product.aborrow.mapper.BorrowInterestMapper;
import com.tianli.product.aborrow.service.BorrowInterestService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
@Service
public class BorrowInterestServiceImpl implements BorrowInterestService {
    @Resource
    private BorrowInterestMapper interestMapper;
    @Resource
    private BorrowInterestLogMapper interestLogMapper;

    @Override
    @Transactional
    public void add(Long bid, Long uid, String coin, BigDecimal amount) {
        BorrowInterest borrowInterest = getAndInitBorrowInterest(bid, uid, coin);

        if (interestMapper.casIncrease(uid, coin, amount, borrowInterest.getAmount()) != 1) {
            throw ErrorCodeEnum.BORROW_INTEREST_EXIST.generalException();
        }

        var log = BorrowInterestLog.builder()
                .amount(amount)
                .uid(uid)
                .bid(bid)
                .coin(coin).build();
        interestLogMapper.insert(log);
    }


    public BorrowInterest getAndInitBorrowInterest(Long bid, Long uid, String coin) {
        BorrowInterest borrowInterest = interestMapper.selectOne(new LambdaQueryWrapper<BorrowInterest>()
                .eq(BorrowInterest::getBid, bid)
                .eq(BorrowInterest::getUid, uid)
                .eq(BorrowInterest::getCoin, coin)
        );


        if (Optional.ofNullable(borrowInterest).isEmpty()) {

            var otherBorrowInterests = interestMapper.selectList(new LambdaQueryWrapper<BorrowInterest>()
                    .ne(BorrowInterest::getBid, bid)
                    .eq(BorrowInterest::getUid, uid)
                    .eq(BorrowInterest::getCoin, coin)
            );

            if (CollectionUtils.isNotEmpty(otherBorrowInterests)) {
                throw ErrorCodeEnum.BORROW_INTEREST_EXIST.generalException();
            }

            borrowInterest = BorrowInterest.builder()
                    .bid(bid).uid(uid).coin(coin).amount(BigDecimal.ZERO).build();
            interestMapper.insert(borrowInterest);
        }

        return borrowInterest;

    }


}
