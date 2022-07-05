package com.tianli.management.agentmanage.controller;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class TeamBetVO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 用户username
     */
    private String uid_username;

    /**
     * 用户昵称
     */
    private String uid_nick;

    /**
     * 押注类型
     */
    private BetTypeEnum bet_type;

    /**
     * 竞猜时间,单位:分钟
     */
    private Double bet_time;

    /**
     * 押注金额
     */
    private double amount;

    /**
     * 押注方向
     */
    private KlineDirectionEnum bet_direction;

    /**
     * 最终方向
     */
    private KlineDirectionEnum final_direction;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;

    /**
     * 平台净利润(用户净利润负数) 总净盈亏
     */
    private double platform_profit;

    /**
     * 代理商抽成
     */
    private double agent_profit;

    private double surplus_profit;

    /**
     * 平台返佣
     */
    private double platform_rebate;

    /**
     * 手续费用BF抵扣时, 消耗的BF(原始的)
     */
    private double base_BF;

    /**
     * 手续费用BF抵扣时, 消耗的BF(打折后)
     */
    private double final_BF;

    /**
     * 手续费用BF抵扣时, 消耗的BF(打折后)
     */
    private CurrencyTokenEnum token;

    /**
     * 优惠金额(USDT)
     */
    private double discount_amount;

    /**
     * 平台抽成
     */
    private double pf_profit;

    public static TeamBetVO trans(Bet bet) {
        TeamBetVO vo = new TeamBetVO();
        BeanUtils.copyProperties(bet, vo);
        CurrencyTokenEnum token = bet.getProfit_token();
        TokenCurrencyType tokenCurrencyType;
        if(token == CurrencyTokenEnum.usdt_omni) {
            tokenCurrencyType = TokenCurrencyType.usdt_omni;
        } else {
            tokenCurrencyType = TokenCurrencyType.BF_bep20;
        }
        vo.setToken(token);
        vo.setAmount(TokenCurrencyType.usdt_omni.money(bet.getAmount()));
        vo.setAgent_profit(Objects.nonNull(bet.getAgent_profit()) ? tokenCurrencyType.money(bet.getAgent_profit()) : 0);
        vo.setPlatform_profit(Objects.nonNull(bet.getPlatform_profit()) ? tokenCurrencyType.money(bet.getPlatform_profit()) : 0);
        vo.setPf_profit(Objects.nonNull(bet.getPf_profit()) ? tokenCurrencyType.money(bet.getPf_profit()) : 0);
        vo.setPlatform_rebate(Objects.nonNull(bet.getPlatform_rebate()) ? tokenCurrencyType.money(bet.getPlatform_rebate()) : 0);
        vo.setBase_BF(Objects.nonNull(bet.getBase_BF()) ? CurrencyTokenEnum.BF_bep20.money(bet.getBase_BF()) : 0);
        vo.setFinal_BF(Objects.nonNull(bet.getFinal_BF()) ? CurrencyTokenEnum.BF_bep20.money(bet.getFinal_BF()) : 0);
        vo.setSurplus_profit(Objects.nonNull(bet.getSurplus_profit()) ? tokenCurrencyType.money(bet.getSurplus_profit()) : 0);
        vo.setDiscount_amount(Objects.nonNull(bet.getDiscount_amount()) ? TokenCurrencyType.usdt_omni.money(bet.getDiscount_amount()) : 0);
        return vo;
    }
}
