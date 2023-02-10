package com.tianli.product.aborrow.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.service.CurrencyService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.BorrowRecordSnapshotDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.entity.BorrowRecordSnapshot;
import com.tianli.product.aborrow.mapper.BorrowRecordMapper;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.service.*;
import org.springframework.stereotype.Service;

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
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord>
        implements BorrowRecordService {

    @Resource
    private CurrencyService currencyService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowRecordSnapshotService borrowRecordSnapshotService;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;

    @Override
    public void calPledgeRate(Long uid, Boolean autoReplenishment) {
        BorrowRecord borrowRecord = getAndInit(uid, autoReplenishment);
        if (!autoReplenishment.equals(borrowRecord.isAutoReplenishment())) {
            borrowRecord.setAutoReplenishment(autoReplenishment);
        }

        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid);
        var coinRates = currencyService.rateMap();

        PledgeRateDto pledgeRateDto = this.calPledgeRate(coinRates, borrowRecordPledges, borrowRecordCoins);
        borrowRecord.setLqPledgeRate(pledgeRateDto.getLqPledgeRate());
        borrowRecord.setCurrencyPledgeRate(pledgeRateDto.getCurrencyPledgeRate());

        var snapshot = generateSnapshot(uid, borrowRecordCoins, borrowRecordPledges, pledgeRateDto, coinRates);

        borrowRecord.setNewestSSnapshotId(snapshot.getId());

        this.save(borrowRecord);
    }

    @Override
    public PledgeRateDto preCalPledgeRate(Long uid, BorrowCoinQuery borrowCoinQuery) {

        List<BorrowRecordCoin> borrowRecordCoins = borrowRecordCoinService.listByUid(uid);
        var borrowRecordPledges = borrowRecordPledgeService.dtoListByUid(uid);

        BorrowRecordCoin borrowRecordCoin = BorrowRecordCoin.builder()
                .amount(borrowCoinQuery.getBorrowAmount())
                .coin(borrowCoinQuery.getBorrowCoin()).build();

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
        BigDecimal LqAmount = BigDecimal.ZERO;
        BigDecimal warnAmount = BigDecimal.ZERO;
        BigDecimal assureLqAmount = BigDecimal.ZERO;


        for (BorrowRecordPledgeDto dto : borrowRecordPledgeDtos) {
            var borrowConfigPledge = borrowConfigPledgeService.getById(dto.getCoin());

            BigDecimal userPledgeAmount = dto.getAmount();
            pledgeAmount = pledgeAmount.add(userPledgeAmount.multiply(rateMapFinal.get(dto.getCoin())));
            LqAmount = LqAmount.add(userPledgeAmount.multiply(borrowConfigPledge.getLqPledgeRate()));
            warnAmount = warnAmount.add(userPledgeAmount.multiply(borrowConfigPledge.getWarnPledgeRate()));
            assureLqAmount = assureLqAmount.add(userPledgeAmount.multiply(borrowConfigPledge.getAssureLqPledgeRate()));
        }

        BigDecimal borrowAmount = borrowRecordCoins.stream()
                .map(record -> record.getAmount().multiply(rateMapFinal.get(record.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interestAmount = BigDecimal.ZERO; // todo 利息计算
        var currencyPledgeRate = borrowAmount.add(interestAmount)
                .divide(pledgeAmount, 8, RoundingMode.UP);


        var lqPledgeRate = LqAmount.divide(pledgeAmount, 8, RoundingMode.UP);
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


    private BorrowRecord getAndInit(Long uid, Boolean autoReplenishment) {
        BorrowRecord borrowRecord = this.getOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUid, uid));
        if (Objects.isNull(borrowRecord)) {
            borrowRecord = BorrowRecord.builder()
                    .uid(uid)
                    .autoReplenishment(autoReplenishment)
                    .build();
            this.save(borrowRecord);
        }
        return borrowRecord;
    }
}

