package com.tianli.chain.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.tianli.chain.mapper.ChainTx;
import com.tianli.chain.mapper.ChainTxMapper;
import com.tianli.chain.service.ChainTxService;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/11/16 21:24
 */

@RestController
@RequestMapping("/chain/tx")
public class ChainTxController {

    @GetMapping("/list")
    public Result list(int page, int size,
                       TokenCurrencyType currencyType, ChargeStatus status) {
        IPage<ChainTx> iPage = new Page<>(page, size);
        iPage = chainTxMapper.selectPage(iPage, new LambdaQueryWrapper<ChainTx>()
                .eq(currencyType != null, ChainTx::getCurrency_type, currencyType).eq(status!=null, ChainTx::getStatus,status)
                .orderByDesc(ChainTx::getId));
        return Result.instance().setData(MapTool.Map().put("count", iPage.getTotal()).put("list", iPage.getRecords().stream().map(e -> {
            ChainTxDTO chainTxDTO = new ChainTxDTO();
            BeanUtils.copyProperties(e, chainTxDTO);
            DigitalCurrency digitalCurrency = new DigitalCurrency(e.getCurrency_type(), e.getAmount());
            chainTxDTO.setMoney(digitalCurrency.getMoney());
            chainTxDTO.setCny(digitalCurrency.toOther(TokenCurrencyType.cny).getMoney());
            return chainTxDTO;
        }).collect(Collectors.toList())));
    }


    @Resource
    private ChainTxMapper chainTxMapper;
    @Resource
    private ChainTxService chainTxService;
    @Resource
    private Gson gson;
    @Resource
    private ConfigService configService;
}
