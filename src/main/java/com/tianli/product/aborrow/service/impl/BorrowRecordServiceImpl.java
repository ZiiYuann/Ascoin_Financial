package com.tianli.product.aborrow.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.service.CurrencyService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.dto.BorrowRecordSnapshotDto;
import com.tianli.product.aborrow.dto.PledgeRateDto;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import com.tianli.product.aborrow.entity.BorrowRecordSnapshot;
import com.tianli.product.aborrow.mapper.BorrowRecordMapper;
import com.tianli.product.aborrow.service.*;
import org.springframework.stereotype.Service;

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
        var borrowCoinSet =
                borrowRecordCoins.stream().map(BorrowRecordCoin::getCoin).collect(Collectors.toSet());
        var pledgeCoinSet =
                borrowRecordPledges.stream().map(BorrowRecordPledgeDto::getCoin).collect(Collectors.toSet());

        var coinSet = new HashSet<String>();
        coinSet.addAll(borrowCoinSet);
        coinSet.addAll(pledgeCoinSet);
        var coinRates = getRateMap(coinSet);

        BigDecimal borrowAmount = borrowRecordCoins.stream()
                .map(record -> record.getAmount().multiply(coinRates.get(record.getCoin())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PledgeRateDto pledgeRateDto = this.calPledgeRate(coinRates, borrowRecordPledges);
        BigDecimal interestAmount = BigDecimal.ZERO; // todo 利息计算


        var currencyPledgeRate = borrowAmount.add(interestAmount)
                .divide(pledgeRateDto.getPledgeAmount(), 8, RoundingMode.UP);
        var lqPledgeRate = pledgeRateDto.getLqPledgeRate();
        borrowRecord.setLqPledgeRate(lqPledgeRate);
        borrowRecord.setCurrencyPledgeRate(currencyPledgeRate);

        Long snapshotId = generateSnapshot(uid, borrowRecordCoins, borrowRecordPledges, coinRates);
        borrowRecord.setNewestSSnapshotId(snapshotId);

        this.save(borrowRecord);
    }

    @Override
    public PledgeRateDto calPledgeRate(HashMap<String, BigDecimal> rateMap
            , List<BorrowRecordPledgeDto> borrowRecordPledgeDtos) {
        if (Objects.isNull(rateMap)) {
            Set<String> coins =
                    borrowRecordPledgeDtos.stream().map(BorrowRecordPledgeDto::getCoin).collect(Collectors.toSet());
            rateMap = getRateMap(coins);
        }

        final var rateMapFinal = rateMap;

        BigDecimal pledgeAmount = BigDecimal.ZERO;
        BigDecimal LqBorrowAmount = BigDecimal.ZERO;

        for (BorrowRecordPledgeDto dto : borrowRecordPledgeDtos) {
            pledgeAmount = pledgeAmount.add(dto.getAmount().multiply(rateMapFinal.get(dto.getCoin())));
            var borrowConfigPledge = borrowConfigPledgeService.getById(dto.getCoin());
            LqBorrowAmount = LqBorrowAmount.add(dto.getAmount().multiply(borrowConfigPledge.getLqPledgeRate()));
        }

        var lqPledgeRate = LqBorrowAmount.divide(pledgeAmount, 8, RoundingMode.UP);

        return PledgeRateDto.builder()
                .lqPledgeRate(lqPledgeRate)
                .LqBorrowAmount(lqPledgeRate)
                .pledgeAmount(pledgeAmount).build();
    }

    private Long generateSnapshot(Long uid, List<BorrowRecordCoin> borrowRecordCoins, List<BorrowRecordPledgeDto> borrowRecordPledges, HashMap<String, BigDecimal> coinRates) {
        // Snapshot
        BorrowRecordSnapshotDto borrowRecordSnapshotDto = BorrowRecordSnapshotDto.builder()
                .borrowRecordPledgeDtos(borrowRecordPledges)
                .borrowRecordCoins(borrowRecordCoins)
                .coinRates(coinRates).build();
        BorrowRecordSnapshot borrowRecordSnapshot = BorrowRecordSnapshot.builder()
                .uid(uid)
                .data(JSONUtil.toJsonStr(borrowRecordSnapshotDto)).build();
        borrowRecordSnapshotService.save(borrowRecordSnapshot);
        return borrowRecordSnapshot.getId();
    }

    private HashMap<String, BigDecimal> getRateMap(Set<String> coins) {
        var coinRates = new HashMap<String, BigDecimal>();
        coins.forEach(coin -> coinRates.put(coin, currencyService.getDollarRate(coin)));
        return coinRates;
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

