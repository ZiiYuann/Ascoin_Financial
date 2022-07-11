package com.tianli.chain.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.chain.mapper.ChainLog;
import com.tianli.chain.mapper.ChainLogMapper;
import com.tianli.chain.mapper.ChainTx;
import com.tianli.chain.service.ChainTxService;
import com.tianli.charge.ChargeService;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import com.tianli.tool.MapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/11/16 17:07
 */

@RestController
@RequestMapping("/chain")
public class ChainController {

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.待归集管理)
    public Result list(int page, int size,
                       CurrencyAdaptType currencyAdaptType, String username, String address) {
        IPage<ChainLog> iPage = new Page<>(page, size);
        iPage = chainLogMapper.selectPage(iPage, new LambdaQueryWrapper<ChainLog>()
                .eq(!StringUtils.isEmpty(username), ChainLog::getUsername, username)
                .like(!StringUtils.isEmpty(address), ChainLog::getAddress, address)
                .eq(currencyAdaptType != null, ChainLog::getCurrency_type, currencyAdaptType)
                .orderByDesc(ChainLog::getId)
        );
        return Result.instance().setData(MapTool.Map().put("count", iPage.getTotal())
                .put("list", iPage.getRecords().stream().map(e -> {
                    ChainLogDTO chainLogDTO = new ChainLogDTO();
                    BeanUtils.copyProperties(e, chainLogDTO);
                    DigitalCurrency digitalCurrency = new DigitalCurrency(e.getCurrency_type(), e.getAmount());
                    chainLogDTO.setMoney(digitalCurrency.getMoney());
                    return chainLogDTO;
                }).collect(Collectors.toList())));
    }

    @GetMapping("/collect/page")
    @AdminPrivilege(and = Privilege.主链交易明细)
    public Result page(int page, int size, String address, String txid, String startTime, String endTime) {
        Page<ChainTx> chainTxPage = chainTxService.page(page, size, address, txid, startTime, endTime, ChargeStatus.chain_success);
        // 总的提现金额
        double withdrawTotalAmount = chargeService.totalWithdrawAmount();
        // 总的归集金额
        double chainTxTotalAmount = chainTxService.totalAmount();
        return Result.instance().setData(MapTool.Map().put("count", chainTxPage.getTotal())
                .put("list", chainTxPage.getRecords().stream().map(ChainTxDetailVO::trans).collect(Collectors.toList()))
                .put("stat", MapTool.Map()
                        .put("withdrawAmount", withdrawTotalAmount)
                        .put("collectAmount", chainTxTotalAmount)
                        .put("mainChainAmount", 0.0)
                        .put("mainChainOriginalAmount", 0.0)
                )
        );
    }

    @PostMapping("/{id}/collect")
    @AdminPrivilege(and = Privilege.待归集管理)
    public Result collect(@PathVariable long id) {
        ChainLog chainLog = chainLogMapper.selectById(id);
        if (chainLog == null) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        ChainLog other = chainLogMapper.selectOne(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, chainLog.getAddress())
                .eq(ChainLog::getCurrency_type, other(chainLog.getCurrency_type())));
        chainTxService.collect(chainLog.getCurrency_type(), chainLog.getAmount(), chainLog.getAddress(), chainLog.getUid(),
                other == null ? BigInteger.ZERO : other.getAmount());
        return Result.instance();
    }


    private CurrencyAdaptType other(CurrencyAdaptType currencyAdaptType) {
        switch (currencyAdaptType) {
            case usdt_omni:
                return CurrencyAdaptType.btc;
            case btc:
                return CurrencyAdaptType.usdt_omni;
            case usdt_erc20:
                return CurrencyAdaptType.eth;
            case eth:
                return CurrencyAdaptType.usdt_erc20;
            case usdt_trc20:
                return CurrencyAdaptType.tron;
        }
        ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return null;
    }


    @Resource
    private ChainTxService chainTxService;
    @Resource
    private ChainLogMapper chainLogMapper;
    @Resource
    private ChargeService chargeService;
}
