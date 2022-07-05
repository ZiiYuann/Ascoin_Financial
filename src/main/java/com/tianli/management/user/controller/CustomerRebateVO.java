package com.tianli.management.user.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.rebate.mapper.Rebate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerRebateVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 下注金额
     */
    private double amount;

    /**
     * 下注用户id
     */
    private Long uid;

    /**
     * 下注用户名
     */
    private String phone;

    /**
     * 返佣金额
     */
    private double rebate_amount;

    private CurrencyTokenEnum token;

    public static CustomerRebateVO trans(Rebate rebate) {
        TokenCurrencyType tokenCurrencyType;
        if(rebate.getToken() == CurrencyTokenEnum.usdt_omni) {
            tokenCurrencyType = TokenCurrencyType.usdt_omni;
        } else {
            tokenCurrencyType = TokenCurrencyType.BF_bep20;
        }
        return CustomerRebateVO.builder()
                .id(rebate.getId())
                .create_time(rebate.getCreate_time())
                .amount(TokenCurrencyType.usdt_omni.money(rebate.getAmount()))
                .uid(rebate.getUid())
                .phone(rebate.getUid_username())
                .rebate_amount(tokenCurrencyType.money(rebate.getRebate_amount()))
                .token(rebate.getToken())
                .build();

    }

}
