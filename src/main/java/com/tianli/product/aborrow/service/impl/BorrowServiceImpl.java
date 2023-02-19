package com.tianli.product.aborrow.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.charge.enums.ChargeType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.BorrowRecordSnapshotDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.*;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.CalPledgeQuery;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private BorrowOperationLogService borrowOperationLogService;
    @Resource
    private BorrowInterestService borrowInterestService;

    @Override
    @Transactional
    public void borrowCoin(Long uid, BorrowCoinQuery query) {
        borrowConfigCoinService.check(uid, query);

        this.preCalPledgeRate(uid, CalPledgeQuery.builder()
                .pledgeContext(query.getPledgeContext())
                .borrowCoin(query.getBorrowCoin())
                .borrowAmount(query.getBorrowAmount())
                .borrow(true).build());


        BorrowRecord borrowRecord = borrowRecordService.getAndInit(uid, query.getAutoReplenishment());
        Long bid = borrowRecord.getId();

        query.getPledgeContext().forEach(context -> borrowRecordPledgeService.save(uid, bid, context));
        borrowRecordCoinService.save(uid, bid, query);

        this.insertOperationLog(bid, ChargeType.borrow
                , uid, query.getBorrowCoin(), query.getBorrowAmount());
    }

    @Override
    @Transactional
    public void repayCoin(Long uid, RepayCoinQuery query) {
        BorrowRecord borrowRecord = borrowRecordService.getValid(uid);
        borrowRecordCoinService.repay(uid, borrowRecord.getId(), query);

        this.insertOperationLog(borrowRecord.getId(), ChargeType.repay
                , uid, query.getCoin(), query.getAmount());
    }

    @Override
    public void modifyPledgeContext(Long uid, ModifyPledgeContextQuery query) {

        BorrowRecord borrowRecord = borrowRecordService.getValid(uid);

        query.getPledgeContext().forEach(pledgeContext -> {
            borrowRecordPledgeService.save(uid, borrowRecord.getId(), pledgeContext, query.getType());

            ChargeType chargeType = ModifyPledgeContextType.ADD.equals(query.getType()) ?
                    ChargeType.pledge : ChargeType.release;

            this.insertOperationLog(borrowRecord.getId(), chargeType
                    , uid, pledgeContext.getCoin(), pledgeContext.getPledgeAmount());
        });
    }

    @Override
    @Transactional
    public BorrowRecord calPledgeRate(BorrowRecord borrowRecord, Long uid, Boolean autoReplenishment) {
        if (!autoReplenishment.equals(borrowRecord.isAutoReplenishment())) {
            borrowRecord.setAutoReplenishment(autoReplenishment);
        }

        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid);
        List<BorrowInterest> borrowInterests = borrowInterestService.list(uid);

        final Set<String> borrowCoins = borrowRecordCoins.stream().map(BorrowRecordCoin::getCoin).collect(Collectors.toSet());
        final Set<String> pledgeCoins = borrowRecordPledges.stream().map(BorrowRecordPledgeDto::getCoin).collect(Collectors.toSet());

        var coinRates = currencyService.rateMap(new ArrayList<>() {{
            addAll(borrowCoins);
            addAll(pledgeCoins);
        }});

        PledgeRateDto pledgeRateDto = this.calPledgeRate(coinRates, borrowRecordPledges, borrowRecordCoins, borrowInterests, true);
        borrowRecord.setLqPledgeRate(pledgeRateDto.getLqPledgeRate());
        borrowRecord.setCurrencyPledgeRate(pledgeRateDto.getCurrencyPledgeRate());
        borrowRecord.setAssureLqPledgeRate(pledgeRateDto.getAssureLqPledgeRate());
        borrowRecord.setWarnPledgeRate(pledgeRateDto.getWarnPledgeRate());
        borrowRecord.setBorrowCoins(StringUtils.join(borrowCoins, ","));
        borrowRecord.setPledgeCoins(StringUtils.join(pledgeCoins, ","));
        borrowRecord.setBorrowFee(pledgeRateDto.getBorrowFee());
        borrowRecord.setPledgeFee(pledgeRateDto.getPledgeFee());
        borrowRecord.setInterestFee(pledgeRateDto.getInterestFee());

        var snapshot = generateSnapshot(uid, borrowRecordCoins, borrowRecordPledges
                , pledgeRateDto, coinRates, borrowRecord.getId());

        borrowRecord.setNewestSnapshotId(snapshot.getId());

        borrowRecordService.saveOrUpdate(borrowRecord);
        return borrowRecord;
    }

    @Override
    public PledgeRateDto preCalPledgeRate(Long uid, CalPledgeQuery calPledgeQuery) {

        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid);
        List<BorrowInterest> borrowInterests = borrowInterestService.list(uid);

        BorrowConfigCoin borrowConfigCoin = borrowConfigCoinService.getById(calPledgeQuery.getBorrowCoin());
        if (Objects.isNull(borrowConfigCoin)) {
            throw ErrorCodeEnum.BORROW_COIN_NOT_OPEN.generalException();
        }

        BorrowRecordCoin borrowRecordCoin = BorrowRecordCoin.builder()
                .amount(calPledgeQuery.getBorrowAmount())
                .coin(calPledgeQuery.getBorrowCoin()).build();

        borrowRecordCoins.add(borrowRecordCoin);

        calPledgeQuery.getPledgeContext().forEach(context -> {
            if (PledgeType.FINANCIAL.equals(context.getPledgeType())) {
                Long recordId = context.getRecordId();
                FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);
                BorrowRecordPledgeDto pledgeDto = BorrowRecordPledgeDto.builder()
                        .coin(financialRecord.getCoin())
                        .amount(financialRecord.getHoldAmount())
                        .build();
                borrowRecordPledges.add(pledgeDto);
            }

            if (PledgeType.WALLET.equals(context.getPledgeType())) {
                BorrowRecordPledgeDto pledgeDto = BorrowRecordPledgeDto.builder()
                        .coin(context.getCoin())
                        .amount(context.getPledgeAmount())
                        .build();
                borrowRecordPledges.add(pledgeDto);
            }

        });

        return calPledgeRate(null, borrowRecordPledges, borrowRecordCoins, borrowInterests, false);

    }

    @Override
    public PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos
            , List<BorrowRecordCoin> borrowRecordCoins
            , List<BorrowInterest> borrowInterests
            , boolean borrow
    ) {


        if (Objects.isNull(rateMap)) {
            final Set<String> borrowCoins = borrowRecordCoins.stream().map(BorrowRecordCoin::getCoin).collect(Collectors.toSet());
            final Set<String> pledgeCoins = borrowRecordPledgeDtos.stream().map(BorrowRecordPledgeDto::getCoin).collect(Collectors.toSet());

            rateMap = currencyService.rateMap(new ArrayList<>() {{
                addAll(borrowCoins);
                addAll(pledgeCoins);
            }});
        }

        final var rateMapFinal = rateMap;

        BigDecimal pledgeFee = BigDecimal.ZERO;
        BigDecimal initFee = BigDecimal.ZERO;
        BigDecimal warnFee = BigDecimal.ZERO;
        BigDecimal lqFee = BigDecimal.ZERO;
        BigDecimal assureLqFee = BigDecimal.ZERO;
        BigDecimal interestFee = BigDecimal.ZERO;

        for (BorrowInterest borrowInterest : borrowInterests) {
            interestFee = interestFee.add(borrowInterest.getAmount().multiply(rateMapFinal.get(borrowInterest.getCoin())));
        }

        List<AmountDto> pledgeDtos = borrowRecordPledgeDtos.stream()
                .collect(Collectors.groupingBy(BorrowRecordPledgeDto::getCoin)).entrySet()
                .stream().map(entry -> {
                    AmountDto amountDto = new AmountDto();
                    amountDto.setAmount(entry.getValue().stream().map(BorrowRecordPledgeDto::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    amountDto.setCoin(entry.getKey());
                    return amountDto;
                }).collect(Collectors.toList());


        for (AmountDto dto : pledgeDtos) {
            var borrowConfigPledge = borrowConfigPledgeService.getById(dto.getCoin());
            // 质押金额
            BigDecimal userPledgeFee = dto.getAmount().multiply(rateMapFinal.get(dto.getCoin()));
            pledgeFee = pledgeFee.add(userPledgeFee);
            lqFee = lqFee.add(userPledgeFee.multiply(borrowConfigPledge.getLqPledgeRate()));
            warnFee = warnFee.add(userPledgeFee.multiply(borrowConfigPledge.getWarnPledgeRate()));
            assureLqFee = assureLqFee.add(userPledgeFee.multiply(borrowConfigPledge.getAssureLqPledgeRate()));
            initFee = initFee.add(userPledgeFee.multiply(borrowConfigPledge.getInitPledgeRate()));
        }

        if (pledgeFee.compareTo(BigDecimal.ZERO) == 0) {
            return new PledgeRateDto();
        }

        BigDecimal borrowAmount = borrowRecordCoins.stream()
                .map(index -> index.getAmount().multiply(rateMapFinal.get(index.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (borrow && borrowAmount.compareTo(initFee) > 0) {
            throw ErrorCodeEnum.BORROW_EXCEED_INIT.generalException();
        }
        var currencyPledgeRate = borrowAmount.add(interestFee).divide(pledgeFee, 8, RoundingMode.UP);
        var lqPledgeRate = lqFee.divide(pledgeFee, 8, RoundingMode.UP);
        var warnPledgeRate = warnFee.divide(pledgeFee, 8, RoundingMode.UP);
        var assureLqPledgeRate = warnFee.divide(pledgeFee, 8, RoundingMode.UP);

        return PledgeRateDto.builder()
                .pledgeFee(pledgeFee)
                .LqFee(lqPledgeRate)
                .lqPledgeRate(lqPledgeRate)
                .warnFee(warnFee)
                .warnPledgeRate(warnPledgeRate)
                .assureLqFee(assureLqFee)
                .assureLqPledgeRate(assureLqPledgeRate)
                .borrowFee(borrowAmount)
                .currencyPledgeRate(currencyPledgeRate)
                .interestFee(interestFee)
                .initFee(initFee)
                .build();
    }

    private void insertOperationLog(Long bid, ChargeType chargeType
            , Long uid, String coin, BigDecimal amount) {

        var borrowRecord = borrowRecordService.getById(bid);
        BorrowOperationLog operationLog = BorrowOperationLog.log(chargeType, borrowRecord.getId(), uid, coin, amount);
        operationLog.setPrePledgeRate(borrowRecord.getCurrencyPledgeRate());

        borrowRecord = this.calPledgeRate(borrowRecord, uid, borrowRecord.isAutoReplenishment());

        operationLog.setDisplay(true);
        operationLog.setAfterPledgeRate(borrowRecord.getCurrencyPledgeRate());
        borrowOperationLogService.save(operationLog);
    }

    private BorrowRecordSnapshot generateSnapshot(Long uid
            , List<BorrowRecordCoin> borrowRecordCoins
            , List<BorrowRecordPledgeDto> borrowRecordPledges
            , PledgeRateDto pledgeRateDto
            , HashMap<String, BigDecimal> coinRates
            , Long bid) {
        // Snapshot
        BorrowRecordSnapshotDto borrowRecordSnapshotDto = BorrowRecordSnapshotDto.builder()
                .borrowRecordPledgeDtos(borrowRecordPledges)
                .borrowRecordCoins(borrowRecordCoins)
                .coinRates(coinRates)
                .pledgeRateDto(pledgeRateDto)
                .build();
        BorrowRecordSnapshot borrowRecordSnapshot = BorrowRecordSnapshot.builder()
                .uid(uid)
                .bid(bid)
                .data(JSONUtil.toJsonStr(borrowRecordSnapshotDto)).build();
        borrowRecordSnapshotService.save(borrowRecordSnapshot);
        return borrowRecordSnapshot;
    }
}
