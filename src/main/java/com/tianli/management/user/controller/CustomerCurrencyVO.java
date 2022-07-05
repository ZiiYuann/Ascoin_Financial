package com.tianli.management.user.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 余额变动记录表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CustomerCurrencyVO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 记录类型
     */
    private CurrencyLogType log_type;

    /**
     * 余额变动描述
     */
    private String des;

    /**
     * 金额
     */
    private double amount;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    private CurrencyTokenEnum token;

    public static CustomerCurrencyVO trans(CurrencyLog log) {
        CurrencyLogType log_type = log.getLog_type();
        boolean isNegate = true;
        if(Objects.equals(log_type, CurrencyLogType.increase) || Objects.equals(log_type, CurrencyLogType.unfreeze)){
            isNegate = false;
        }
        TokenCurrencyType tokenCurrencyType;
        if(log.getToken() == CurrencyTokenEnum.usdt_omni) {
            tokenCurrencyType = TokenCurrencyType.usdt_omni;
        } else {
            tokenCurrencyType = TokenCurrencyType.BF_bep20;
        }
        return CustomerCurrencyVO.builder()
                .id(log.getId())
                .log_type(log_type)
                .amount(tokenCurrencyType.money(isNegate ? log.getAmount().negate() : log.getAmount()))
                .des(log.getDes())
                .create_time(log.getCreate_time())
                .token(log.getToken())
                .build();
    }
}
