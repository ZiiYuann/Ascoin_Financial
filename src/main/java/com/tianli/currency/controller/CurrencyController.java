package com.tianli.currency.controller;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DailyGiftRecord;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.currency.mapper.DiscountCurrencyLog;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.dto.CurrencyTokenPage;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.CurrencyToken;
import com.tianli.currency_token.order.TokenDealService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kline.KLineConstants;
import com.tianli.kline.task.KlineStatDTO;
import com.tianli.loan.entity.LoanCurrency;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.UserService;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户余额表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@RestController
@RequestMapping("/currency")
public class CurrencyController {
    @Resource
    private UserService userService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private ConfigService configService;
    @Resource
    private DiscountCurrencyService discountCurrencyService;
    @Resource
    private CurrencyLogService currencyLogService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private UserIpLogService userIpLogService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @Resource
    private Gson gson;

    @GetMapping("/my")
    public Result my() {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        if (Objects.isNull(user) || Objects.equals(user.getStatus(), UserStatus.disable)) {
            ErrorCodeEnum.ACCOUNT_BAND.throwException();
        }
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        Currency financial = currencyService.get(uid, CurrencyTypeEnum.financial);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        DiscountCurrency byId = discountCurrencyService.getById(uid);
        LoanCurrency loanCurrency = loanCurrencyService.findByUid(uid, CurrencyCoinEnum.usdt);
        String daily_rate = configService.getOrDefault(ConfigConstants.USER_BALANCE_DAILY_RATE, "0.00001");
        String bf_discount_rate = configService.getOrDefault(ConfigConstants.USDT_BF_DISCOUNT_RATE, "0.75");
        String bf_usdt_rate = configService.getOrDefault(ConfigConstants.BF_USDT_RATE, "1");
        String bf_switch_min_amount = configService.getOrDefault(ConfigConstants.BF_SWITCH_MIN_AMOUNT, "100");
        List<CurrencyTokenPage> currencyTokenPage = currencyTokenService.list(
                new LambdaQueryWrapper<CurrencyToken>().gt(CurrencyToken::getBalance, BigDecimal.ZERO)
                        .eq(CurrencyToken::getUid, uid)
        ).stream().map(o -> {
            BigDecimal price = tokenDealService.getBianPrice(CurrencyCoinEnum.usdt, o.getToken());
            return CurrencyTokenPage.builder()
                    .token(o.getToken())
                    .balance(o.getBalance().setScale(6, RoundingMode.FLOOR).toString())
                    .remain(o.getRemain().setScale(6, RoundingMode.FLOOR).toString())
                    .freeze(o.getFreeze().setScale(6, RoundingMode.FLOOR).toString())
                    .value_balance(o.getBalance().multiply(price))
                    .value_freeze(o.getFreeze().multiply(price))
                    .value_remain(o.getRemain().multiply(price))
                    .build();
        }).collect(Collectors.toList());//.mapToDouble(o -> Double.parseDouble(o.getValue_u())).sum()).setScale(6, RoundingMode.HALF_DOWN).toString();

        String actual_balance = String.valueOf(currencyTokenPage.stream().mapToDouble(o -> o.getValue_balance().doubleValue()).sum());
        String actual_freeze = String.valueOf(currencyTokenPage.stream().mapToDouble(o -> o.getValue_freeze().doubleValue()).sum());
        String actual_remain = String.valueOf(currencyTokenPage.stream().mapToDouble(o -> o.getValue_remain().doubleValue()).sum());

        CurrencyToken currencyUsdt = currencyTokenService.get(uid, CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt);


        return Result.instance().setData(MapTool.Map()
                .put("remain", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("balance", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("freeze", TokenCurrencyType.usdt_omni.money(currency.getFreeze()))
                .put("daily_rate", Double.parseDouble(daily_rate))
                .put("weak_balance", Objects.isNull(byId) ? 0 : TokenCurrencyType.usdt_omni.money(byId.getBalance()))
                .put("loan_balance", loanCurrency.getBalance())
                .put("remain_BF", CurrencyTokenEnum.BF_bep20.money(currency.getRemain_BF()))
                .put("balance_BF", CurrencyTokenEnum.BF_bep20.money(currency.getBalance_BF()))
                .put("freeze_BF", CurrencyTokenEnum.BF_bep20.money(currency.getFreeze_BF()))
                .put("BF_discount_rate", Double.parseDouble(bf_discount_rate))
                .put("BF_switch_min_amount", Double.parseDouble(bf_switch_min_amount))
                .put("BF_switch_status", user.getBF())
                .put("BF_usdt_rate", bf_usdt_rate)
                .put("financial_balance", TokenCurrencyType.usdt_omni.money(financial.getBalance()))
                .put("financial_remain", TokenCurrencyType.usdt_omni.money(financial.getRemain()))
                .put("financial_freeze", TokenCurrencyType.usdt_omni.money(financial.getFreeze()))
                .put("actual_balance", actual_balance)
                .put("actual_remain", actual_remain)
                .put("actual_freeze", actual_freeze)
                .put("actual_balance_usdt", currencyUsdt.getBalance())
                .put("actual_remain_usdt", currencyUsdt.getRemain())
                .put("actual_freeze_usdt", currencyUsdt.getFreeze())
        );
    }

    @GetMapping("/my/remain/{type}")
    public Result remainOfType(@PathVariable("type") TokenCurrencyType type) {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        String WITHDRAW_RATE = null;
        String WITHDRAW_FIXED_AMOUNT = null;
        String WITHDRAW_MIN_AMOUNT = null;
        if (TokenCurrencyType.usdt_erc20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDT_ERC20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDT_ERC20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDT_ERC20_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.usdt_trc20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDT_TRC20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDT_TRC20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDT_TRC20_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.usdt_omni == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDT_OMNI_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDT_OMNI_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDT_OMNI_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.usdt_bep20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDT_BEP20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDT_BEP20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDT_BEP20_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.usdc_trc20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDC_TRC20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDC_TRC20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDC_TRC20_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.usdc_bep20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDC_BEP20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDC_BEP20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDC_BEP20_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.usdc_erc20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.USDC_ERC20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.USDC_ERC20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.USDC_ERC20_WITHDRAW_MIN_AMOUNT);
        } else if (TokenCurrencyType.BF_bep20 == type) {
            WITHDRAW_RATE = configService.getNoCache(ConfigConstants.BF_BEP20_WITHDRAW_RATE);
            WITHDRAW_FIXED_AMOUNT = configService.getNoCache(ConfigConstants.BF_BEP20_WITHDRAW_FIXED_AMOUNT);
            WITHDRAW_MIN_AMOUNT = configService.getNoCache(ConfigConstants.BF_BEP20_WITHDRAW_MIN_AMOUNT);
        } else {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        double rate = Double.parseDouble(WITHDRAW_RATE);
        double minAmount = type.money(new BigInteger(WITHDRAW_MIN_AMOUNT));
        double fixedAmount = type.money(new BigInteger(WITHDRAW_FIXED_AMOUNT));
        double money;
        if (TokenCurrencyType.BF_bep20 == type) {
            money = TokenCurrencyType.BF_bep20.money(currency.getRemain_BF());
        } else {
            money = TokenCurrencyType.usdt_omni.money(currency.getRemain());
        }

        return Result.instance().setData(MapTool.Map()
                .put("remain", money)
                .put("rate", rate)
                .put("fixedAmount", fixedAmount)
                .put("minAmount", minAmount)
        );
    }

    @GetMapping("/log/page")
    public Result logPage(LogPageDTO dto) {
        Long uid = requestInitService.uid();
        LambdaQueryWrapper<CurrencyLog> queryWrapper = new LambdaQueryWrapper<CurrencyLog>()
                .eq(CurrencyLog::getUid, uid)
                .in(CurrencyLog::getLog_type, Lists.newArrayList(CurrencyLogType.reduce, CurrencyLogType.increase, CurrencyLogType.withdraw))
                .in(CurrencyLog::getType, ListUtil.of(CurrencyTypeEnum.normal,CurrencyTypeEnum.loan))
                .orderByDesc(CurrencyLog::getId);
        if (ObjectUtil.isNotNull(dto.getDes())) {
            List<CurrencyLogDes> currencyLogDes;
            if (dto.getDes().equals(CurrencyLogDes.充值)) {
                currencyLogDes = Lists.newArrayList(CurrencyLogDes.充值, CurrencyLogDes.线下充值);
            } else if (dto.getDes().equals(CurrencyLogDes.提现)) {
                currencyLogDes = Lists.newArrayList(CurrencyLogDes.提现, CurrencyLogDes.线下提现);
            } else {
                currencyLogDes = Lists.newArrayList(dto.getDes());
            }
            queryWrapper.in(CurrencyLog::getDes, currencyLogDes);
        }
        Page<CurrencyLog> page = currencyLogService.page(new Page<>(dto.getPage(), dto.getSize()), queryWrapper);
        List<LogPageVO> voList = page.getRecords().stream().map(LogPageVO::trans).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map().put("list", voList).put("total", page.getTotal()));
    }

    @GetMapping("/mining/page")
    public Result mining(@RequestParam(value = "page", defaultValue = "1") Integer page,
                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        Page<CurrencyLog> miningPage = currencyLogService.page(new Page<>(page, size), new LambdaQueryWrapper<CurrencyLog>()
                .eq(CurrencyLog::getType, CurrencyTypeEnum.normal)
                .eq(CurrencyLog::getDes, CurrencyLogDes.利息.name())
                .eq(CurrencyLog::getUid, uid)
                .orderByDesc(CurrencyLog::getId));
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        List<LogPageVO> voList = miningPage.getRecords().stream().map(LogPageVO::trans).collect(Collectors.toList());
        BigInteger totalMining = currencyLogService.sumMiningAmount(uid);
        return Result.instance().setData(MapTool.Map()
                .put("totalAmount", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("list", voList)
                .put("totalMining", TokenCurrencyType.usdt_omni.money(totalMining))
        );
    }

    @Resource
    private CurrencyTokenService currencyTokenService;
    @Resource
    private TokenDealService tokenDealService;

}

