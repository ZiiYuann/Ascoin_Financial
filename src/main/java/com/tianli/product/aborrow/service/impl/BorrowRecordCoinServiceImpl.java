package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowInterest;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.enums.InterestType;
import com.tianli.product.aborrow.mapper.BorrowRecordCoinMapper;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.aborrow.vo.BorrowRecordCoinVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowRecordCoinServiceImpl extends ServiceImpl<BorrowRecordCoinMapper, BorrowRecordCoin>
        implements BorrowRecordCoinService {

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private OrderService orderService;
    @Resource
    private BorrowConfigCoinService borrowConfigCoinService;
    @Resource
    private BorrowInterestService borrowInterestService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private CoinBaseService coinBaseService;


    @Override
    @Transactional
    public void save(Long uid, Long bid, BorrowCoinQuery query) {
        String coin = query.getBorrowCoin();
        BorrowRecordCoin borrowRecordCoin = getAndInit(uid, coin, bid);
        this.casIncrease(borrowRecordCoin.getId(), coin, query.getBorrowAmount(), borrowRecordCoin.getAmount());

        Order order = Order.success(uid, ChargeType.borrow, coin, query.getBorrowAmount(), bid);
        orderService.save(order);

        accountBalanceService.increase(uid, ChargeType.borrow, coin, query.getBorrowAmount(), order.getOrderNo(), "借币");

        BorrowConfigCoin borrowConfigCoin = borrowConfigCoinService.getById(coin);
        BigDecimal hourRate = borrowConfigCoin.getHourRate();
        borrowInterestService.add(bid, uid, coin, hourRate.multiply(query.getBorrowAmount()));
    }


    @Override
    @Transactional
    public void repay(Long uid, Long bid, RepayCoinQuery query) {
        BorrowInterest borrowInterest = borrowInterestService.get(uid, bid, query.getCoin());
        BorrowRecordCoin borrowRecordCoin = getValid(uid, bid, query.getCoin());
        BigDecimal repayAmount = query.getAmount();

        if (repayAmount.compareTo(borrowRecordCoin.getAmount()
                .add(borrowInterest == null ? BigDecimal.ZERO : borrowInterest.getAmount())) > 0) {
            throw ErrorCodeEnum.BORROW_REPAY_AMOUNT_ERROR.generalException();
        }

        Order order = Order.success(uid, ChargeType.repay, query.getCoin(), repayAmount, bid);
        orderService.save(order);
        accountBalanceService.decrease(uid, ChargeType.borrow, query.getCoin(), repayAmount, order.getOrderNo(), ChargeType.repay.getNameZn());

        if (Objects.nonNull(borrowInterest) && borrowInterest.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            var reduceInterestAmount = repayAmount.compareTo(borrowInterest.getAmount()) >= 0
                    ? borrowInterest.getAmount() : repayAmount;
            borrowInterestService.reduce(bid, uid, query.getCoin(), reduceInterestAmount);
            repayAmount = repayAmount.subtract(reduceInterestAmount);
        }

        if (repayAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.casDecrease(borrowRecordCoin.getId(), query.getCoin(), repayAmount, borrowRecordCoin.getAmount());
        }

        // 释放质押物
        if (repayAmount.compareTo(borrowRecordCoin.getAmount()) == 0) {
            borrowRecordPledgeService.release(uid, bid);
            borrowRecordService.finish(uid, bid);
        }

    }

    @Override
    public boolean payOff(Long uid, Long bid) {
        Optional<BorrowRecordCoin> any = this.list(new LambdaQueryWrapper<BorrowRecordCoin>()
                        .eq(BorrowRecordCoin::getUid, uid)
                        .eq(BorrowRecordCoin::getBid, bid)).stream()
                .filter(borrowRecordCoin -> borrowRecordCoin.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .findAny();

        return any.isPresent();
    }

    @Override
    @Transactional
    public void calInterest(Long uid, Long bid) {
        List<BorrowRecordCoin> recordCoins = this.list(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getBid, bid));
        recordCoins.forEach(recordCoin -> {
            String coin = recordCoin.getCoin();
            var hourRate = borrowConfigCoinService.getById(coin).getHourRate();
            BigDecimal amount = recordCoin.getAmount();
            borrowInterestService.add(bid, uid, coin, amount.subtract(hourRate), InterestType.HOUR);
        });
    }

    @Override
    public List<BorrowRecordCoin> listByUid(Long uid, Long bid) {
        return this.list(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getBid, bid));
    }

    @Override
    public List<BorrowRecordCoinVO> vos(Long uid, Long bid) {
        List<BorrowRecordCoin> recordCoins = this.listByUid(uid, bid);
        return recordCoins.stream().map(recordCoin -> BorrowRecordCoinVO.builder()
                .coin(recordCoin.getCoin())
                .amount(recordCoin.getAmount())
                .logo(coinBaseService.getByName(recordCoin.getCoin()).getLogo())
                .interestAmount(borrowInterestService.get(uid, bid, recordCoin.getCoin()).getAmount())
                .build()).collect(Collectors.toList());
    }

    @Override
    public BorrowRecordCoin getOne(Long uid, String coin) {
        return this.getOne(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getCoin, coin));
    }


    private void casIncrease(Long id, String coin, BigDecimal increaseAmount, BigDecimal originalAmount) {
        int i = baseMapper.casIncrease(id, coin, increaseAmount, originalAmount);
        if (i != 1) {
            throw ErrorCodeEnum.BORROW_RECORD_COIN_ERROR.generalException();
        }
    }

    private void casDecrease(Long id, String coin, BigDecimal decreaseAmount, BigDecimal originalAmount) {
        int i = baseMapper.casDecrease(id, coin, decreaseAmount, originalAmount);
        if (i != 1) {
            throw ErrorCodeEnum.BORROW_RECORD_COIN_ERROR.generalException();
        }
    }

    private BorrowRecordCoin getValid(Long uid, Long bid, String coin) {
        BorrowRecordCoin borrowRecordCoin = this.getOne(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getCoin, coin)
                .eq(BorrowRecordCoin::getBid, bid));
        return Optional.ofNullable(borrowRecordCoin).orElseThrow(ErrorCodeEnum.BORROW_RECORD_NOT_EXIST::generalException);
    }

    private BorrowRecordCoin getAndInit(Long uid, String coin, Long bid) {
        BorrowRecordCoin borrowRecordCoin = this.getOne(new LambdaQueryWrapper<BorrowRecordCoin>()
                .eq(BorrowRecordCoin::getUid, uid)
                .eq(BorrowRecordCoin::getCoin, coin)
                .eq(BorrowRecordCoin::getBid, bid));

        if (Objects.isNull(borrowRecordCoin)) {
            borrowRecordCoin = BorrowRecordCoin.builder()
                    .uid(uid)
                    .coin(coin)
                    .bid(bid)
                    .amount(BigDecimal.ZERO)
                    .build();
            this.save(borrowRecordCoin);
        }
        return borrowRecordCoin;
    }
}
