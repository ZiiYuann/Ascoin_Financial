package com.tianli.account.controller;

import com.tianli.account.entity.AccountSummary;
import com.tianli.account.service.AccountSummaryService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.vo.AddressVO;
import com.tianli.common.init.RequestInitService;
import com.tianli.account.enums.ProductType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote 用户账户控制器
 * @since 2022-07-06
 **/
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AddressService addressService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private UserService userService;
    @Resource
    private AccountSummaryService accountBalanceService;

    /**
     * 激活钱包
     */
    @GetMapping("/{productType}/activate")
    public Result activateWallet(@PathVariable String productType){
        Long uid = requestInitService.uid();
        ProductType type = ProductType.getInstance(productType);
        addressService.getAndInit(uid, type);
        return Result.success();
    }

    /**
     * 查看钱包
     */
    @GetMapping("/{productType}")
    public Result walletStatus(@PathVariable String productType){
        Long uid = requestInitService.uid();
        ProductType type = ProductType.getInstance(productType);
        Address address = addressService.getAndInit(uid, type);
        if(Objects.isNull(address)){
            ErrorCodeEnum.ACCOUNT_NOT_ACTIVE.throwException();
        }
        return Result.success(AddressVO.trans(address));
    }

    /**
     * 查询用户云钱包余额
     */
    @GetMapping("/financial/balance")
    public Result accountBalance() {
        Long uid = requestInitService.uid();
        User user = userService.getEnableUser(uid);

        AccountSummary financial = accountBalanceService.getAndInit(uid, ProductType.financial);

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

}
