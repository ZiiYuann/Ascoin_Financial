package com.tianli.currency.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
public class LogPageVO {

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 创建时间
     */
    private Long create_time_ms;

    /**
     * 剩余余额
     */
    private double remain;

    /**
     * 金额
     */
    private double amount;

    /**
     * 余额变动描述
     */
    private String des;

    /**
     * 余额变动描述 en
     */
    private String enDes;
    private String thaiDes;

    private String token;

    public static LogPageVO trans(AccountBalanceOperationLog currencyLog) {
        BigInteger amount = currencyLog.getAmount();
        if (CurrencyLogType.increase != currencyLog.getLog_type()) {
            amount = amount.negate();
        }
        CurrencyTokenEnum token = currencyLog.getToken();
        double money;
        double remain;
        if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            money = CurrencyTokenEnum.BF_bep20.money(amount);
            remain = CurrencyTokenEnum.BF_bep20.money(currencyLog.getRemain());
        } else {
            money = TokenCurrencyType.usdt_omni.money(amount);
            remain = TokenCurrencyType.usdt_omni.money(currencyLog.getRemain());
        }
        LocalDateTime create_time = currencyLog.getCreate_time();
        Instant create_instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        if (currencyLog.getDes().equals(CurrencyLogDes.线下充值.name())) {
            currencyLog.setDes(CurrencyLogDes.充值.name());
        } else if (currencyLog.getDes().equals(CurrencyLogDes.线下提现.name())) {
            currencyLog.setDes(CurrencyLogDes.提现.name());
        }
        return LogPageVO.builder()
                .create_time(create_time)
                .create_time_ms(create_instant.toEpochMilli())
                .amount(money)
                .remain(remain)
                .des(currencyLog.getDes())
                .enDes(cacheEnDes.get(currencyLog.getDes()))
                .thaiDes(cacheThaiDes.get(currencyLog.getDes()))
                .token(tokenDes.get(token.name()))
                .build();
    }

    private static Map<String, String> cacheEnDes;
    private static Map<String, String> cacheThaiDes;
    private static Map<String, String> tokenDes;

    static {
        cacheEnDes = new HashMap<>();
        cacheEnDes.put("充值", "Deposit");
        cacheEnDes.put("提现", "Withdraw");
        cacheEnDes.put("提现手续费", "Withdraw charge");
        cacheEnDes.put("交易", "Trade");
        cacheEnDes.put("利息", "Interest");
        cacheEnDes.put("余额", "Balance");
        cacheEnDes.put("划入", "In");
        cacheEnDes.put("划出", "Out");
        cacheEnDes.put("买入", "Buy");
        cacheEnDes.put("赎回", "Redeem");
        cacheEnDes.put("收益", "Profit");
        cacheThaiDes = new HashMap<>();
        cacheThaiDes.put("充值", "เติมเงิน");
        cacheThaiDes.put("提现", "ถอนเงินสด");
        cacheThaiDes.put("提现手续费", "ค่าธรรมเนียมการถอน");
        cacheThaiDes.put("交易", "การซื้อขาย");
        cacheThaiDes.put("利息", "ดอกเบี้ย");
        cacheThaiDes.put("余额", "วงเงิน");
        tokenDes = new HashMap<>();
        tokenDes.put("usdt_omni", "usdt");
        tokenDes.put("usdt_bep20", "usdt");
        tokenDes.put("BF_bep20", "BFSP");
        tokenDes.put("usdc_bep20", "usdc");
        tokenDes.put("usdt_erc20", "usdt");
        tokenDes.put("usdc_erc20", "usdc");
        tokenDes.put("usdc_trc20", "usdc");
        tokenDes.put("usdt_trc20", "usdt");
    }
}
