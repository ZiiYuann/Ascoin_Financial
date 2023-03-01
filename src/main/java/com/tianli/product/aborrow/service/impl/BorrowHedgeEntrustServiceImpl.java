package com.tianli.product.aborrow.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.query.IdsQuery;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.query.BorrowHedgeEntrustIoUQuery;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowHedgeEntrust;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.HedgeStatus;
import com.tianli.product.aborrow.enums.HedgeType;
import com.tianli.product.aborrow.mapper.BorrowHedgeEntrustMapper;
import com.tianli.product.aborrow.query.MBorrowHedgeQuery;
import com.tianli.product.aborrow.service.BorrowHedgeEntrustService;
import com.tianli.product.aborrow.service.BorrowRecordPledgeService;
import com.tianli.product.aborrow.vo.MBorrowHedgeEntrustVO;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.LiquidateDTO;
import com.tianli.rpc.dto.LiquidateFillResponse;
import com.tianli.rpc.dto.LiquidateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class BorrowHedgeEntrustServiceImpl extends ServiceImpl<BorrowHedgeEntrustMapper, BorrowHedgeEntrust>
        implements BorrowHedgeEntrustService {

    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private RpcService rpcService;

    @Override
    @Transactional
    public BorrowHedgeEntrust manual(BorrowHedgeEntrustIoUQuery query) {
        Long brId = query.getBrId();
        BorrowRecordPledge borrowRecordPledge = borrowRecordPledgeService.getById(brId);
        BigDecimal entrustRate = query.getEntrustRate();

        // 市价
        BorrowHedgeEntrust borrowHedgeEntrust = BorrowHedgeEntrust.builder()
                .bid(borrowRecordPledge.getBid())
                .brId(brId)
                .createRate(currencyService.getDollarRate(borrowRecordPledge.getCoin()))
                .coin(borrowRecordPledge.getCoin())
                .hedgeCoin("usdt")
                .amount(borrowRecordPledge.getAmount())
                .entrustRate(entrustRate)
                .hedgeType(HedgeType.MANUAL)
                .hedgeStatus(HedgeStatus.WAIT)
                .build();
        baseMapper.insert(borrowHedgeEntrust);
        return borrowHedgeEntrust;
    }

    @Override
    public IPage<MBorrowHedgeEntrustVO> vos(Page<BorrowHedgeEntrust> page, MBorrowHedgeQuery query) {
        return this.page(page, QueryWrapperUtils.generate(BorrowHedgeEntrust.class, query))
                .convert(index -> borrowConvert.toMBorrowHedgeEntrustVO(index));
    }

    @Override
    public void cancel(IdsQuery query) {
        int i = baseMapper.cancel(query.getId());
        if (i != 1) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    @Override
    @Transactional
    public void liquidate(BorrowHedgeEntrust hedge) {
        if (!HedgeStatus.WAIT.equals(hedge.getHedgeStatus())) {
            return;
        }

        BorrowHedgeEntrust borrowHedgeEntrust = this.getById(hedge.getId());
        BigDecimal currencyRate = currencyService.getDollarRate(hedge.getCoin());
        BigDecimal entrustRate = hedge.getEntrustRate();

        BigDecimal specificValue = currencyRate.divide(entrustRate, 8, RoundingMode.DOWN);
        if (specificValue.compareTo(BigDecimal.valueOf(0.999f)) < 0 ||
                specificValue.compareTo(BigDecimal.valueOf(1.01f)) > 0) {
            return;
        }

        String liquidateId = rpcService.liquidate(LiquidateDTO.builder()
                .recordId(hedge.getId())
                .coin(hedge.getCoin())
                .amount(hedge.getAmount())
                .toCoin(hedge.getHedgeCoin())
                .build());

        borrowHedgeEntrust.setHedgeStatus(HedgeStatus.PROCESS);
        borrowHedgeEntrust.setLiquidateId(liquidateId);
        this.updateById(borrowHedgeEntrust);
    }

    @Override
    @Transactional
    public void liquidateStatus(BorrowHedgeEntrust hedge) {
        if (!HedgeStatus.PROCESS.equals(hedge.getHedgeStatus())) {
            return;
        }

        String response = rpcService.liquidateResponse(hedge.getId(), hedge.getLiquidateId());
        JSON parse = JSONUtil.parse(response);
        Integer status = parse.getByPath("data.status", Integer.class);
        if (status != 1) {
            return;
        }
        var liquidateResponse = parse.getByPath("data.binance_res", LiquidateResponse.class);
        List<LiquidateFillResponse> fills = liquidateResponse.getFills();

        BigDecimal translateAmount = BigDecimal.ZERO;
        BigDecimal translatePrice = BigDecimal.ZERO;
        for (LiquidateFillResponse fill : fills) {
            BigDecimal price = fill.getPrice();
            BigDecimal qty = fill.getQty();
            translatePrice = translatePrice.add(qty.multiply(price));
            translateAmount = translateAmount.add(qty);
        }

        hedge.setTranslateAmount(translateAmount);
        hedge.setTranslateRate(translatePrice.divide(translateAmount, 6, RoundingMode.DOWN));
        hedge.setHedgeStatus(HedgeStatus.FINISH);
        this.updateById(hedge);
    }

}
