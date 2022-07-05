package com.tianli.management.currency;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class CurrencyLogVO {

    /**
     * 用户id
     */
    private Long uid;

    private String username;

    /**
     * 币种类型
     */
    private CurrencyTokenEnum token;

    /**
     * 余额变动描述
     */
    private String des;

    /**
     * 类型
     * 0充值 / 1提现
     */
    private Integer type;

    /**
     * 金额
     */
    private Double amount;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    public static CurrencyLogVO convert(CurrencyLog currencyLog, String username) {
        CurrencyLogVO currencyLogVO = new CurrencyLogVO();
        currencyLogVO.setUid(currencyLog.getUid());
        currencyLogVO.setUsername(username);
        currencyLogVO.setToken(currencyLog.getToken());
        currencyLogVO.setDes(currencyLog.getDes());
        currencyLogVO.setDes(currencyLog.getDes());
        currencyLogVO.setType(Objects.equals(currencyLog.getDes(), CurrencyLogDes.充值.name()) ? 0 : 1);
        currencyLogVO.setCreate_time(currencyLog.getCreate_time());
        currencyLogVO.setAmount(CurrencyTokenEnum.usdt_omni.money(currencyLog.getAmount()));
        return currencyLogVO;
    }
}
