package com.tianli.rebate.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.rebate.mapper.Rebate;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * <p>
 * 返佣表
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RebatePageVO {

    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    private Long create_time_ms;

    /**
     * 下注金额
     */
    private double amount;

    /**
     * 下注用户名
     */
    private String uid_username;

    /**
     * 下注用户昵称
     */
    private String uid_nick;

    /**
     * 下注用户头像
     */
    private String uid_avatar;

    /**
     * 返佣金额
     */
    private double rebate_amount;
    private CurrencyTokenEnum token;

    public static RebatePageVO trans(Rebate rebate) {
        CurrencyTokenEnum token = rebate.getToken();
        double rebate_amount;
        if(Objects.equals(token, CurrencyTokenEnum.usdt_omni)){
            rebate_amount = TokenCurrencyType.usdt_omni.money(rebate.getRebate_amount());
        }else{
            rebate_amount = token.money(rebate.getRebate_amount());
        }
        LocalDateTime create_time = rebate.getCreate_time();
        Instant create_instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        return RebatePageVO.builder()
                .create_time(create_time)
                .create_time_ms(create_instant.toEpochMilli())
                .amount(TokenCurrencyType.usdt_omni.money(rebate.getAmount()))
                .uid_username(rebate.getUid_username())
                .uid_nick(rebate.getUid_nick())
                .uid_avatar(rebate.getUid_avatar())
                .rebate_amount(rebate_amount)
                .token(token)
                .build();
    }
}
