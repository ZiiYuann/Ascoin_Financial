package com.tianli.product.aborrow.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.currency.service.CurrencyService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.BorrowRecordSnapshotDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.entity.BorrowRecordSnapshot;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.CalPledgeQuery;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowServiceImpl implements BorrowService {

    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowConfigCoinService borrowConfigCoinService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;
    @Resource
    private BorrowRecordSnapshotService borrowRecordSnapshotService;

    @Override
    @Transactional
    public void borrowCoin(Long uid, BorrowCoinQuery query) {
        borrowConfigCoinService.check(uid, query);

        BorrowRecord borrowRecord = borrowRecordService.getAndInit(uid, query.getAutoReplenishment());
        Long bid = borrowRecord.getId();

        query.getPledgeContext().forEach(context -> borrowRecordPledgeService.save(uid, bid, context));
        borrowRecordCoinService.save(uid, bid, query);

        this.calPledgeRate(uid, query.getAutoReplenishment());

    }

    @Override
    @Transactional
    public void calPledgeRate(Long uid, Boolean autoReplenishment) {
        BorrowRecord borrowRecord = borrowRecordService.getAndInit(uid, autoReplenishment);
        if (!autoReplenishment.equals(borrowRecord.isAutoReplenishment())) {
            borrowRecord.setAutoReplenishment(autoReplenishment);
        }

        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid);
        var coinRates = currencyService.rateMap();

        PledgeRateDto pledgeRateDto = this.calPledgeRate(coinRates, borrowRecordPledges, borrowRecordCoins);
        borrowRecord.setLqPledgeRate(pledgeRateDto.getLqPledgeRate());
        borrowRecord.setCurrencyPledgeRate(pledgeRateDto.getCurrencyPledgeRate());
        borrowRecord.setAssureLqPledgeRate(pledgeRateDto.getAssureLqPledgeRate());
        borrowRecord.setWarnPledgeRate(pledgeRateDto.getWarnPledgeRate());

        var snapshot = generateSnapshot(uid, borrowRecordCoins, borrowRecordPledges, pledgeRateDto, coinRates);

        borrowRecord.setNewestSnapshotId(snapshot.getId());

        borrowRecordService.save(borrowRecord);
    }

    @Override
    public PledgeRateDto preCalPledgeRate(Long uid, CalPledgeQuery calPledgeQuery) {

        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid);

        BorrowRecordCoin borrowRecordCoin = BorrowRecordCoin.builder()
                .amount(calPledgeQuery.getBorrowAmount())
                .coin(calPledgeQuery.getBorrowCoin()).build();

        borrowRecordCoins.add(borrowRecordCoin);

        calPledgeQuery.getPledgeContext().forEach(context -> {
            if (PledgeType.FINANCIAL.equals(context.getPledgeType())) {
                List<Long> recordIds = context.getRecordIds();
                recordIds.forEach(recordId -> {
                    FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);
                    BorrowRecordPledgeDto pledgeDto = BorrowRecordPledgeDto.builder()
                            .coin(financialRecord.getCoin())
                            .amount(financialRecord.getHoldAmount())
                            .build();
                    borrowRecordPledges.add(pledgeDto);
                });
            }

            if (PledgeType.WALLET.equals(context.getPledgeType())) {
                BorrowRecordPledgeDto pledgeDto = BorrowRecordPledgeDto.builder()
                        .coin(context.getCoin())
                        .amount(context.getPledgeAmount())
                        .build();
                borrowRecordPledges.add(pledgeDto);
            }

        });

        return calPledgeRate(null, borrowRecordPledges, borrowRecordCoins);

    }

    @Override
    public PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos
            , List<BorrowRecordCoin> borrowRecordCoins
    ) {


        if (Objects.isNull(rateMap)) {
            rateMap = currencyService.rateMap();
        }

        final var rateMapFinal = rateMap;

        BigDecimal pledgeAmount = BigDecimal.ZERO;
        BigDecimal lqAmount = BigDecimal.ZERO;
        BigDecimal warnAmount = BigDecimal.ZERO;
        BigDecimal assureLqAmount = BigDecimal.ZERO;


        for (BorrowRecordPledgeDto dto : borrowRecordPledgeDtos) {
            var borrowConfigPledge = borrowConfigPledgeService.getById(dto.getCoin());

            BigDecimal userPledgeAmount = dto.getAmount();
            pledgeAmount = pledgeAmount.add(userPledgeAmount.multiply(rateMapFinal.get(dto.getCoin())));
            lqAmount = lqAmount.add(userPledgeAmount.multiply(borrowConfigPledge.getLqPledgeRate()));
            warnAmount = warnAmount.add(userPledgeAmount.multiply(borrowConfigPledge.getWarnPledgeRate()));
            assureLqAmount = assureLqAmount.add(userPledgeAmount.multiply(borrowConfigPledge.getAssureLqPledgeRate()));
        }

        BigDecimal borrowAmount = borrowRecordCoins.stream()
                .map(index -> index.getAmount().multiply(rateMapFinal.get(index.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interestAmount = BigDecimal.ZERO; // todo 利息计算
        var currencyPledgeRate = borrowAmount.add(interestAmount)
                .divide(pledgeAmount, 8, RoundingMode.UP);


        var lqPledgeRate = lqAmount.divide(pledgeAmount, 8, RoundingMode.UP);
        var warnPledgeRate = warnAmount.divide(pledgeAmount, 8, RoundingMode.UP);
        var assureLqPledgeRate = warnAmount.divide(pledgeAmount, 8, RoundingMode.UP);

        return PledgeRateDto.builder()
                .pledgeAmount(pledgeAmount)
                .LqAmount(lqPledgeRate)
                .lqPledgeRate(lqPledgeRate)
                .warnAmount(warnAmount)
                .warnPledgeRate(warnPledgeRate)
                .assureLqAmount(assureLqAmount)
                .assureLqPledgeRate(assureLqPledgeRate)
                .borrowAmount(borrowAmount)
                .currencyPledgeRate(currencyPledgeRate)
                .build();
    }

    private BorrowRecordSnapshot generateSnapshot(Long uid
            , List<BorrowRecordCoin> borrowRecordCoins
            , List<BorrowRecordPledgeDto> borrowRecordPledges
            , PledgeRateDto pledgeRateDto
            , HashMap<String, BigDecimal> coinRates) {
        // Snapshot
        BorrowRecordSnapshotDto borrowRecordSnapshotDto = BorrowRecordSnapshotDto.builder()
                .borrowRecordPledgeDtos(borrowRecordPledges)
                .borrowRecordCoins(borrowRecordCoins)
                .coinRates(coinRates)
                .pledgeRateDto(pledgeRateDto)
                .build();
        BorrowRecordSnapshot borrowRecordSnapshot = BorrowRecordSnapshot.builder()
                .uid(uid)
                .data(JSONUtil.toJsonStr(borrowRecordSnapshotDto)).build();
        borrowRecordSnapshotService.save(borrowRecordSnapshot);
        return borrowRecordSnapshot;
    }
}
