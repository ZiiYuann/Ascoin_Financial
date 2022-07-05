package com.tianli.dividends.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.Currency;
import com.tianli.dividends.DividendsService;
import com.tianli.dividends.mapper.Dividends;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.LowSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import com.tianli.dividends.settlement.mapper.ChargeSettlementType;
import com.tianli.dividends.settlement.mapper.LowSettlement;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import com.tianli.user.mapper.UserIdentity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 分红表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@RestController
@RequestMapping("/dividends")
public class DividendsController {

    @GetMapping("/page")
    public Result page(@RequestParam(value = "page",defaultValue = "1") Integer page, @RequestParam(value = "size",defaultValue = "10") Integer size){
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(uid);
        if(Objects.isNull(agent)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
//        List<Agent> list = agentService.list(new LambdaQueryWrapper<Agent>().eq(Agent::getSenior_id, uid));
//        BigInteger lowBalance = BigInteger.ZERO;
//        if(!CollectionUtils.isEmpty(list)){
//            List<Long> ids = list.stream().map(Agent::getId).collect(Collectors.toList());
//            List<Currency> currencies = currencyService.list(new LambdaQueryWrapper<Currency>().eq(Currency::getType, CurrencyTypeEnum.deposit).in(Currency::getId, ids));
//            lowBalance = currencies.stream().map(Currency::getBalance).reduce(BigInteger::add).orElse(BigInteger.ZERO);
//        }
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        Page<Dividends> dividendsPage = dividendsService.page(new Page<>(page, size), new LambdaQueryWrapper<Dividends>().eq(Dividends::getDividends_uid, uid).orderByDesc(Dividends::getCreate_time));
        List<DividendsVO> vos = dividendsPage.getRecords().stream().map(DividendsVO::trans).collect(Collectors.toList());
        DigitalCurrency digitalCurrency = new DigitalCurrency(TokenCurrencyType.usdt_omni, currency.getBalance());
        DigitalCurrency digitalCurrencyCny = digitalCurrency.toOther(TokenCurrencyType.cny);
//        DigitalCurrency myBalance = new DigitalCurrency(TokenCurrencyType.usdt_omni, currency.getBalance().subtract(lowBalance));
//        DigitalCurrency digitalCurrency_ = myBalance.toOther(TokenCurrencyType.cny);
        BigInteger sumDividends = dividendsService.sumAmount(uid);
        DigitalCurrency digitalCurrency_ = new DigitalCurrency(TokenCurrencyType.usdt_omni, sumDividends).toOther(TokenCurrencyType.cny);
        LinkedList<Agent> agents = agentService.agentChain(uid);
        double dividendsOfAllRate = agents.stream()
                .map(Agent::getReal_dividends)
                .reduce(1.0, (a, b) -> a * b);
        return Result.success(MapTool.Map()
                .put("dividendsRate", agent.getReal_dividends())
                .put("dividendsOfAllRate", dividendsOfAllRate)
                .put("allBalance", digitalCurrency.getMoney())
                .put("allBalanceCny", digitalCurrencyCny.getMoney())
                .put("myBalance", TokenCurrencyType.usdt_omni.money(sumDividends))
                .put("myBalanceCny", digitalCurrency_.getMoney())
                .put("list", vos));
    }

    @GetMapping("/settlement/page")
    public Result settlementPage(@RequestParam(value = "page",defaultValue = "1") Integer page, @RequestParam(value = "size",defaultValue = "10") Integer size){
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(uid);
        if(Objects.isNull(agent)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        List<SettlementVO> settlementVOList;
        if(UserIdentity.senior_agent == agent.getIdentity()){
            Page<ChargeSettlement> chargeSettlementPage = chargeSettlementService.page(new Page<>(page, size), new LambdaQueryWrapper<ChargeSettlement>()
                    .eq(ChargeSettlement::getUid, uid)
                    .eq(ChargeSettlement::getCharge_type, ChargeSettlementType.withdraw)
                    .eq(ChargeSettlement::getStatus, ChargeSettlementStatus.transaction_success)
                    .orderByDesc(ChargeSettlement::getId));
            settlementVOList = chargeSettlementPage.getRecords().stream().map(e -> {
                LocalDateTime create_time = e.getCreate_time();
                Instant instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
                return SettlementVO.builder()
                        .amount(e.getCurrency_type().money(e.getReal_amount()))
                        .create_time(create_time)
                        .create_time_ms(instant.toEpochMilli())
                        .build();
            }).collect(Collectors.toList());
        } else {
            Page<LowSettlement> lowSettlementPage = lowSettlementService.page(new Page<>(page, size), new LambdaQueryWrapper<LowSettlement>()
                    .eq(LowSettlement::getLow_uid, uid)
                    .orderByDesc(LowSettlement::getId));
            settlementVOList = lowSettlementPage.getRecords().stream().map(e -> SettlementVO.builder().amount(TokenCurrencyType.usdt_omni.money(e.getAmount())).create_time(e.getCreate_time()).build()).collect(Collectors.toList());
        }
        return Result.instance().setData(MapTool.Map().put("list", settlementVOList));
    }

    @Resource
    private AgentService agentService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DividendsService dividendsService;

    @Resource
    private ChargeSettlementService chargeSettlementService;

    @Resource
    private LowSettlementService lowSettlementService;

    @Resource
    private RequestInitService requestInitService;

}

