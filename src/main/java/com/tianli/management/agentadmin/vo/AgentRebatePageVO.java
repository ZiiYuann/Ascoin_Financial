package com.tianli.management.agentadmin.vo;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.rebate.mapper.Rebate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chensong
 * @date 2021-01-07 14:20
 * @since 1.0.0
 */
@Data
@Builder
public class AgentRebatePageVO {
    private Long id;
    private String uid_username;
    private String uid_nick;
    private Long bet_id;
    private double amount;
    private double rebate_amount;
    private CurrencyTokenEnum token;
    private LocalDateTime create_time;

    public static AgentRebatePageVO trans(Rebate rebate){
        CurrencyTokenEnum token = rebate.getToken();
        double rebateAmount;
        if(Objects.equals(token, CurrencyTokenEnum.BF_bep20)){
            rebateAmount = Objects.nonNull(rebate.getRebate_amount()) ? TokenCurrencyType.BF_bep20.money(rebate.getRebate_amount()) : 0;
        }else{
            rebateAmount = Objects.nonNull(rebate.getRebate_amount()) ? TokenCurrencyType.usdt_omni.money(rebate.getRebate_amount()) : 0;
        }
        return builder().id(rebate.getId())
                .uid_nick(rebate.getUid_nick()).uid_username(rebate.getUid_username()).bet_id(rebate.getBet_id())
                .amount(Objects.nonNull(rebate.getAmount())? TokenCurrencyType.usdt_omni.money(rebate.getAmount()):0)
                .rebate_amount(rebateAmount)
                .token(token)
                .create_time(rebate.getCreate_time()).build();
    }
}
