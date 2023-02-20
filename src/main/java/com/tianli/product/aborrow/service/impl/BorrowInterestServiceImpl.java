package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowInterest;
import com.tianli.product.aborrow.entity.BorrowInterestLog;
import com.tianli.product.aborrow.enums.InterestType;
import com.tianli.product.aborrow.mapper.BorrowInterestLogMapper;
import com.tianli.product.aborrow.mapper.BorrowInterestMapper;
import com.tianli.product.aborrow.service.BorrowInterestService;
import com.tianli.tool.time.TimeTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        add(uid, bid, coin, amount, InterestType.BORROW);
    }

    @Override
    public void add(Long bid, Long uid, String coin, BigDecimal amount, InterestType interestType) {
        BorrowInterest borrowInterest = getAndInitBorrowInterest(bid, uid, coin);

        if (interestMapper.casIncrease(borrowInterest.getId(), coin, amount, borrowInterest.getAmount()) != 1) {
            throw ErrorCodeEnum.BORROW_INTEREST_EXIST.generalException();
        }
        LocalDateTime now = LocalDateTime.now();
        var log = BorrowInterestLog.builder()
                .interestType(interestType)
                .interestTime(InterestType.BORROW.equals(interestType) ? now : TimeTool.hour(now))
                .amount(amount)
                .uid(uid)
                .bid(bid)
                .coin(coin).build();
        interestLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void reduce(Long bid, Long uid, String coin, BigDecimal amount) {
        BorrowInterest borrowInterest = getAndInitBorrowInterest(bid, uid, coin);

        if (interestMapper.casDecrease(borrowInterest.getId(), coin, amount, borrowInterest.getAmount()) != 1) {
            throw ErrorCodeEnum.BORROW_INTEREST_EXIST.generalException();
        }
    }

    @Override
    public boolean payOff(Long uid, Long bid) {
        List<BorrowInterest> list = interestMapper.selectList(new LambdaQueryWrapper<BorrowInterest>()
                .eq(BorrowInterest::getUid, uid)
                .eq(BorrowInterest::getBid, bid));
        return list.stream().filter(index -> index.getAmount().compareTo(BigDecimal.ZERO) > 0).findAny()
                .isEmpty();
    }

    @Override
    public List<BorrowInterest> list(Long uid) {
        return interestMapper.selectList(new LambdaQueryWrapper<BorrowInterest>()
                .eq(BorrowInterest::getUid, uid));
    }

    @Override
    public BorrowInterest get(Long uid, Long bid, String coin) {
        return interestMapper.selectOne(new LambdaQueryWrapper<BorrowInterest>()
                .eq(BorrowInterest::getUid, uid)
                .eq(BorrowInterest::getBid, bid)
                .eq(BorrowInterest::getCoin, coin));
    }

    public BorrowInterest getAndInitBorrowInterest(Long bid, Long uid, String coin) {
        BorrowInterest borrowInterest = interestMapper.selectOne(new LambdaQueryWrapper<BorrowInterest>()
                .eq(BorrowInterest::getBid, bid)
                .eq(BorrowInterest::getUid, uid)
                .eq(BorrowInterest::getCoin, coin)
        );

        if (Optional.ofNullable(borrowInterest).isEmpty()) {
            borrowInterest = BorrowInterest.builder()
                    .bid(bid).uid(uid).coin(coin).amount(BigDecimal.ZERO).build();
            interestMapper.insert(borrowInterest);
        }
        return borrowInterest;
    }


}
