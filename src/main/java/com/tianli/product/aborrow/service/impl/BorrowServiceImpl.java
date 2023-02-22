package com.tianli.product.aborrow.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.BorrowRecordSnapshotDTO;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.*;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.query.*;
import com.tianli.product.aborrow.service.*;
import com.tianli.product.aborrow.vo.BorrowRecordSnapshotVO;
import com.tianli.product.aborrow.vo.BorrowRecordVO;
import com.tianli.product.aborrow.vo.HoldBorrowingVO;
import com.tianli.product.aborrow.vo.HoldPledgingVO;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.vo.RateVo;
import com.tianli.tool.ApplicationContextTool;
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
    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchange;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private OrderService orderService;

    @Override
    @Transactional
    public void borrowCoin(Long uid, BorrowCoinQuery query) {
        borrowConfigCoinService.check(uid, query);

        this.preCalPledgeRate(uid, CalPledgeQuery.builder()
                .pledgeContext(query.getPledgeContext())
                .coin(query.getBorrowCoin())
                .amount(query.getBorrowAmount())
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
        Long bid = borrowRecord.getId();
        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid, bid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid, bid);
        List<BorrowInterest> borrowInterests = borrowInterestService.list(uid);

        final Set<String> borrowCoins = borrowRecordCoins.stream().map(BorrowRecordCoin::getCoin).collect(Collectors.toSet());
        final Set<String> pledgeCoins = borrowRecordPledges.stream().map(BorrowRecordPledgeDto::getCoin).collect(Collectors.toSet());

        List<String> coins = new ArrayList<>();
        coins.addAll(borrowCoins);
        coins.addAll(pledgeCoins);
        var coinRates = currencyService.rateMap(coins);

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
    public BorrowRecord calPledgeRate(BorrowRecord borrowRecord) {
        BorrowServiceImpl bean = ApplicationContextTool.getBean(BorrowServiceImpl.class);
        bean = Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
        return bean.calPledgeRate(borrowRecord, borrowRecord.getUid(), borrowRecord.isAutoReplenishment());
    }

    @Override
    public PledgeRateDto preCalPledgeRate(Long uid, CalPledgeQuery calPledgeQuery) {
        BorrowRecord borrowRecord = borrowRecordService.get(uid);
        List<BorrowRecordCoin> borrowRecordCoins =
                borrowRecordCoinService.listByUid(uid, Objects.isNull(borrowRecord) ? null : borrowRecord.getId());
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid, borrowRecord.getId());
        List<BorrowInterest> borrowInterests = borrowInterestService.list(uid);

        BorrowConfigCoin borrowConfigCoin = borrowConfigCoinService.getById(calPledgeQuery.getCoin());
        if (Objects.isNull(borrowConfigCoin)) {
            throw ErrorCodeEnum.BORROW_COIN_NOT_OPEN.generalException();
        }

        BorrowRecordCoin borrowRecordCoin = BorrowRecordCoin.builder()
                .amount(calPledgeQuery.getAmount())
                .coin(calPledgeQuery.getCoin()).build();

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
            List<String> coins = new ArrayList<>();
            coins.addAll(borrowCoins);
            coins.addAll(pledgeCoins);
            rateMap = currencyService.rateMap(coins);
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

    @Override
    public BorrowRecordSnapshotVO newestSnapshot(Long uid) {
        BorrowRecord borrowRecord = borrowRecordService.get(uid);
        if (Objects.isNull(borrowRecord)) {
            return null;
        }

        BorrowRecordSnapshot borrowRecordSnapshot = borrowRecordSnapshotService.getById(borrowRecord.getNewestSnapshotId());

        BorrowRecordSnapshotDTO borrowRecordSnapshotDTO =
                JSONUtil.toBean(borrowRecordSnapshot.getData(), BorrowRecordSnapshotDTO.class);
        BorrowRecordVO borrowRecordVO = borrowConvert.toBorrowRecordVO(borrowRecord);
        Map<String, BigDecimal> rateMap = borrowRecordSnapshotDTO.getCoinRates().stream()
                .collect(Collectors.toMap(AmountDto::getCoin, AmountDto::getAmount));

        List<HoldBorrowingVO> holdBorrowingVOS = borrowRecordSnapshotDTO.getBorrowRecordCoins().stream()
                .map(index -> HoldBorrowingVO.builder()
                        .id(index.getId()).amount(index.getAmount()).coin(index.getCoin())
                        .logo(coinBaseService.getByName(index.getCoin()).getLogo())
                        .hourRate(borrowConfigCoinService.getById(index.getCoin()).getHourRate())
                        .build())
                .sorted((e1, e2) -> e2.getAmount().multiply(rateMap.get(e2.getCoin()))
                        .compareTo(e1.getAmount().multiply(rateMap.get(e1.getCoin())))).collect(Collectors.toList());
        List<HoldPledgingVO> holdPledgingVOS = borrowRecordSnapshotDTO.getBorrowRecordPledgeDtos().stream()
                .map(index -> HoldPledgingVO.builder()
                        .id(index.getId())
                        .amount(index.getAmount())
                        .coin(index.getCoin())
                        .logo(coinBaseService.getByName(index.getCoin()).getLogo())
                        .pledgeType(index.getPledgeType())
                        .fee(index.getAmount().multiply(rateMap.get(index.getCoin())))
                        .build())
                .sorted((e1, e2) -> e2.getAmount().multiply(rateMap.get(e2.getCoin()))
                        .compareTo(e1.getAmount().multiply(rateMap.get(e1.getCoin()))))
                .collect(Collectors.toList());
        var usdtCnyRate = BigDecimal.valueOf(digitalCurrencyExchange.usdtCnyPrice());
        return BorrowRecordSnapshotVO.builder()
                .holdBorrowingVOS(holdBorrowingVOS)
                .holdPledgingVOS(holdPledgingVOS)
                .borrowRecordVO(borrowRecordVO)
                .pledgeRateDto(borrowRecordSnapshotDTO.getPledgeRateDto())
                .coinRates(borrowRecordSnapshotDTO.getCoinRates().stream()
                        .map(dto -> new RateVo(dto.getCoin(), dto.getAmount(), usdtCnyRate))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public void autoReplenishment(BorrowRecord borrowRecord) {

        Long uid = borrowRecord.getUid();
        Long bid = borrowRecord.getId();

        var recordPledges = borrowRecordPledgeService.listByUid(uid, bid);
        var recordCoins = borrowRecordCoinService.listByUid(uid, bid);

        final Set<String> borrowCoins = recordCoins.stream().map(BorrowRecordCoin::getCoin).collect(Collectors.toSet());
        final Set<String> pledgeCoins = recordPledges.stream().map(BorrowRecordPledge::getCoin).collect(Collectors.toSet());

        List<String> coins = new ArrayList<>();
        coins.addAll(borrowCoins);
        coins.addAll(pledgeCoins);
        var rateMap = currencyService.rateMap(coins);

        List<AccountBalance> accountBalances = new ArrayList<>();
        var recordPledgeMap = recordPledges.stream()
                .filter(index -> PledgeType.WALLET.equals(index.getPledgeType()))
                .peek(index -> accountBalances.add(accountBalanceService.getAndInit(uid, index.getCoin())))
                .collect(Collectors.toMap(BorrowRecordPledge::getCoin, o -> o));

        // sort
        accountBalances.sort((e1, e2) -> e2.getRemain().multiply(rateMap.get(e2.getCoin()))
                .compareTo(e1.getRemain().multiply(rateMap.get(e1.getCoin()))));

        var configPledgeMap = borrowConfigPledgeService.listByIds(borrowCoins).stream()
                .collect(Collectors.toMap(BorrowConfigPledge::getCoin, o -> o));

        BigDecimal borrowFee = recordCoins.stream().map(borrow -> borrow.getAmount().multiply(rateMap.get(borrow.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal initFee = recordPledges.stream().map(pledge ->
                        pledge.getAmount().multiply(rateMap.get(pledge.getCoin()))
                                .multiply(configPledgeMap.get(pledge.getCoin()).getInitPledgeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (AccountBalance accountBalance : accountBalances) {
            String coin = accountBalance.getCoin();
            BorrowConfigPledge configPledge = configPledgeMap.get(coin);
            BigDecimal currentPledgeFee = recordPledgeMap.get(coin).getAmount().multiply(rateMap.get(coin));
            BigDecimal otherInitFee =
                    initFee.subtract(currentPledgeFee.multiply(configPledge.getInitPledgeRate()));

            BigDecimal replenishmentAmount = borrowFee.subtract(otherInitFee)
                    .divide(configPledge.getInitPledgeRate(), RoundingMode.UP)
                    .subtract(currentPledgeFee)
                    .divide(rateMap.get(coin), RoundingMode.UP).setScale(8, RoundingMode.UP);


            if (accountBalance.getRemain().compareTo(replenishmentAmount) >= 0) {

                this.modifyPledgeContext(uid, ModifyPledgeContextQuery.builder()
                        .type(ModifyPledgeContextType.ADD)
                        .pledgeContext(List.of(PledgeContextQuery.builder()
                                .pledgeType(PledgeType.WALLET)
                                .coin(coin)
                                .pledgeAmount(replenishmentAmount).build())).build());

                Order order = Order.success(uid, ChargeType.auto_re, coin, replenishmentAmount, bid);
                orderService.save(order);

                this.insertOperationLog(borrowRecord.getId(), ChargeType.auto_re
                        , uid, coin, replenishmentAmount);
                // todo 补仓成功
                return;
            }
        }

        // todo 手动

    }

    @Override
    @Transactional
    public void forcedCloseout(BorrowRecord borrowRecord) {
        Long bid = borrowRecord.getId();
        Long uid = borrowRecord.getUid();
        var recordPledgeDtos = borrowRecordPledgeService.dtoListByUid(uid, bid);
        var recordCoins = borrowRecordCoinService.listByUid(uid, bid);
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
        BorrowRecordSnapshotDTO borrowRecordSnapshotDto = BorrowRecordSnapshotDTO.builder()
                .borrowRecordPledgeDtos(borrowRecordPledges)
                .borrowRecordCoins(borrowRecordCoins)
                .coinRates(coinRates.entrySet().stream()
                        .map(index -> new AmountDto(index.getValue(), index.getKey())).collect(Collectors.toList()))
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
