package com.tianli.management.ruleconfig;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.ruleconfig.mapper.BetDuration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BetDurationVO {

    private Integer id;

    private Double duration;

    private Double min_bet_amount;

    private Double max_bet_amount;

    private Double extra_percentage;

    public static BetDurationVO trans(BetDuration betDuration){
        return BetDurationVO.builder()
                .id(betDuration.getId())
                .duration(betDuration.getDuration())
                .min_bet_amount(TokenCurrencyType.usdt_omni.money(Objects.isNull(betDuration.getMin_bet_amount()) ? BigInteger.ZERO : betDuration.getMin_bet_amount()))
                .max_bet_amount(TokenCurrencyType.usdt_omni.money(Objects.isNull(betDuration.getMax_bet_amount()) ? BigInteger.ZERO : betDuration.getMax_bet_amount()))
                .extra_percentage(betDuration.getExtra_percentage())
                .build();
    }
}
