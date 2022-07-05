package com.tianli.management.fundmanagement;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author cs
 * @Date 2022-01-14 11:44 上午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DividendsManageVO {
    private Long id;
    private String uid_username;
    private String uid_nick;
    private String agent_username;
    private BetTypeEnum bet_type;
    private Double bet_time;
    private LocalDateTime create_time;
    private double amount;
    private KlineDirectionEnum bet_direction;
    private KlineDirectionEnum final_direction;
    private BetResultEnum result;
    private CurrencyTokenEnum profit_token;
    private double agent_profit;
    private double pf_profit;
    private double surplus_profit;

    public static DividendsManageVO trans(Bet bet) {
        TokenCurrencyType tokenCurrencyType;
        if(bet.getProfit_token() == CurrencyTokenEnum.usdt_omni){
            tokenCurrencyType = TokenCurrencyType.usdt_omni;
        } else {
            tokenCurrencyType = TokenCurrencyType.BF_bep20;
        }
        return DividendsManageVO.builder()
                .id(bet.getId()).uid_username(bet.getUid_username())
                .uid_nick(bet.getUid_nick())
                .agent_username(bet.getAgent_username()).bet_type(bet.getBet_type())
                .bet_time(bet.getBet_time()).create_time(bet.getCreate_time())
                .amount(TokenCurrencyType.usdt_omni.money(bet.getAmount()))
                .bet_direction(bet.getBet_direction())
                .final_direction(bet.getFinal_direction()).result(bet.getResult())
                .pf_profit(bet.getPf_profit() == null ? 0.0 : tokenCurrencyType.money(bet.getPf_profit()))
                .agent_profit(bet.getAgent_profit() == null ? 0.0 :tokenCurrencyType.money(bet.getAgent_profit()))
                .surplus_profit(bet.getSurplus_profit() == null ? 0.0 :tokenCurrencyType.money(bet.getSurplus_profit()))
                .profit_token(bet.getProfit_token())
                .build();
    }
}
