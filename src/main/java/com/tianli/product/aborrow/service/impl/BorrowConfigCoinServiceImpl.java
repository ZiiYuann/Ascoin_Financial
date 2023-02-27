package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.enums.BorrowStatus;
import com.tianli.product.aborrow.mapper.BorrowConfigCoinMapper;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.BorrowConfigCoinIoUQuery;
import com.tianli.product.aborrow.query.BorrowQuery;
import com.tianli.product.aborrow.service.BorrowConfigCoinService;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.service.BorrowRecordService;
import com.tianli.product.aborrow.vo.AccountBorrowVO;
import com.tianli.product.aborrow.vo.BorrowConfigCoinVO;
import com.tianli.product.aborrow.vo.MBorrowConfigCoinVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private CurrencyService currencyService;

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
                .convert(index -> {
                            MBorrowConfigCoinVO mBorrowConfigCoinVO = borrowConvert.toMBorrowConfigCoinVO(index);
                            mBorrowConfigCoinVO.setLogo(coinBaseService.getByName(mBorrowConfigCoinVO.getCoin()).getLogo());
                            return mBorrowConfigCoinVO;
                        }
                );
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
    public void check(Long uid, Long bid, BorrowCoinQuery query) {
        BorrowConfigCoin borrowConfigCoin = this.getById(query.getBorrowCoin());

        if (borrowConfigCoin.getStatus() != 1) {
            throw ErrorCodeEnum.BORROW_COIN_NOT_OPEN.generalException();
        }

        BorrowRecordCoin borrowRecordCoin = borrowRecordCoinService.getOne(uid, bid, query.getBorrowCoin());
        BigDecimal borrowAmount = borrowRecordCoin != null ? borrowRecordCoin.getAmount() : BigDecimal.ZERO;

        BigDecimal totalBorrowAmount = borrowAmount.add(query.getBorrowAmount());
        if (totalBorrowAmount.compareTo(borrowConfigCoin.getMinAmount()) < 0) {
            throw ErrorCodeEnum.BORROW_AMOUNT_MIN_ERROR.generalException();
        }

        if (Objects.nonNull(borrowConfigCoin.getMaxAmount()) && totalBorrowAmount.compareTo(borrowConfigCoin.getMaxAmount()) > 0) {
            throw ErrorCodeEnum.BORROW_AMOUNT_MAX_ERROR.generalException();
        }
    }

    @Override
    public List<BorrowConfigCoinVO> getVOs() {
        return this.list(new LambdaQueryWrapper<BorrowConfigCoin>()
                        .eq(BorrowConfigCoin::getStatus, 1))
                .stream().map(index -> {
                    BorrowConfigCoinVO borrowConfigCoinVO = borrowConvert.toBorrowConfigCoinVO(index);
                    borrowConfigCoinVO.setLogo(coinBaseService.getByName(index.getCoin()).getLogo());
                    return borrowConfigCoinVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountBorrowVO> getAccountBorrowVOs(Long uid) {
        List<BorrowConfigCoinVO> coins = getVOs();

        BorrowRecord borrowRecord = borrowRecordService.get(uid);
        return coins.stream().map(index -> {
            String coin = index.getCoin();
            AccountBorrowVO accountBorrowVO = borrowConvert.toAccountBorrowVO(index);
            AccountBalance accountBalance = accountBalanceService.getAndInit(uid, index.getCoin());
            accountBorrowVO.setRemain(accountBalance.getRemain());
            accountBorrowVO.setRate(currencyService.getDollarRate(index.getCoin()));
            accountBorrowVO.setLogo(coinBaseService.getByName(coin).getLogo());

            if (Objects.nonNull(borrowRecord)) {
                BigDecimal dollarRate = currencyService.getDollarRate(coin);
                var borrowRecordCoinMap = borrowRecordCoinService.listByUid(uid, borrowRecord.getId())
                        .stream().collect(Collectors.toMap(BorrowRecordCoin::getCoin, o -> o));

                BorrowRecordCoin borrowRecordCoin = borrowRecordCoinMap.get(coin);
                accountBorrowVO.setBorrowAmount(Objects.isNull(borrowRecordCoin) ? BigDecimal.ZERO
                        : borrowRecordCoin.getAmount());
                accountBorrowVO.setRate(dollarRate);
                accountBorrowVO.setBorrowProportion(accountBorrowVO.getBorrowAmount().multiply(dollarRate)
                        .divide(borrowRecord.getBorrowFee(), 4, RoundingMode.DOWN));
                accountBorrowVO.setHold(Objects.nonNull(borrowRecordCoin));
            }
            return accountBorrowVO;
        }).collect(Collectors.toList());
    }

}
