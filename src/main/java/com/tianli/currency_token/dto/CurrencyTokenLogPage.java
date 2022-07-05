package com.tianli.currency_token.dto;

import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.CurrencyTokenLog;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyTokenLogPage {

    private Long id;

    private Long uid;

    private CurrencyTypeEnum type;

    private CurrencyCoinEnum token;

    private String sn;

    private CurrencyLogType log_type;

    private CurrencyLogDes des;

    private String enDes;

    private BigDecimal amount;

    private LocalDateTime create_time;

    private Long create_time_ms;

    private BigDecimal balance;

    private BigDecimal freeze;

    private BigDecimal remain;

    public static CurrencyTokenLogPage trans(CurrencyTokenLog currencyTokenLog) {
        return CurrencyTokenLogPage.builder()
                .amount(currencyTokenLog.getLog_type().equals(CurrencyLogType.increase) ? currencyTokenLog.getAmount() : BigDecimal.ZERO.subtract(currencyTokenLog.getAmount()))
                .create_time(currencyTokenLog.getCreate_time())
                .create_time_ms(currencyTokenLog.getCreate_time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .des(currencyTokenLog.getDes())
                .enDes(cacheEnDes.get(currencyTokenLog.getDes().name()))
                .balance(currencyTokenLog.getBalance())
                .freeze(currencyTokenLog.getFreeze())
                .remain(currencyTokenLog.getRemain())
                .id(currencyTokenLog.getId())
                .log_type(currencyTokenLog.getLog_type())
                .sn(currencyTokenLog.getSn())
                .token(currencyTokenLog.getToken())
                .uid(currencyTokenLog.getUid())
                .type(currencyTokenLog.getType()).build();
    }

    private static Map<String, String> cacheEnDes;


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
    }

}
