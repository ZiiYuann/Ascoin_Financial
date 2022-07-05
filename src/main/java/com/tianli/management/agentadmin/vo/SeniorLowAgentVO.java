package com.tianli.management.agentadmin.vo;

import com.tianli.agent.mapper.Agent;
import com.tianli.common.DoubleDecimalTrans;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.Currency;
import com.tianli.user.statistics.mapper.UserStatistics;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SeniorLowAgentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 期望押金
     */
    private double deposit;

    /**
     * 代理商总净盈亏
     */
    private double profit;

    /**
     * 已结算数额
     */
    private double settled_number;

    /**
     * 未结算数额
     */
    private double not_settled_number;

    /**
     * 商定分红
     */
    private Double expect_dividends;

    /**
     * 代理商名称
     */
    private String nick;

    /**
     * 代理商手机号
     */
    private String username;

    /**
     * 实际分红
     */
    private Double real_dividends;

    /**
     * 普通场返佣比例
     */
    private Double normal_rebate_proportion;

    /**
     * 期望押金
     */
    private Double expect_deposit;

    /**
     * 稳赚场返佣比例
     */
    private Double steady_rebate_proportion;

//    /**
//     * 是否需要关注
//     */
//    private Boolean focus;

    /**
     * 团队人数
     */
    private Long team_number;

    /**
     * 备注
     */
    private String note;

    /**
     * 推广链接
     */
    private String referral_url;

    public static SeniorLowAgentVO trans(Agent agent, Currency currency, Currency sCurrency, UserStatistics statistics){
        double profit = TokenCurrencyType.usdt_omni.money(agent.getProfit());
        double not_settled_number = Objects.isNull(sCurrency) ? 0 : TokenCurrencyType.usdt_omni.money(sCurrency.getBalance());
        return SeniorLowAgentVO.builder()
                .id(agent.getId())
                .nick(agent.getNick())
                .username(agent.getUsername())
                .create_time(agent.getCreate_time())
                .deposit(Objects.isNull(currency) ? 0 : TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .profit(profit)
                .not_settled_number(not_settled_number)
                .settled_number(profit - not_settled_number)
                .expect_dividends(DoubleDecimalTrans.double_multiply_hundred(agent.getExpect_dividends()))
                .real_dividends(DoubleDecimalTrans.double_multiply_hundred(agent.getReal_dividends()))
                .expect_deposit(TokenCurrencyType.usdt_omni.money(agent.getExpect_deposit()))
                .normal_rebate_proportion(DoubleDecimalTrans.double_multiply_hundred(agent.getNormal_rebate_proportion()))
                .steady_rebate_proportion(DoubleDecimalTrans.double_multiply_hundred(agent.getSteady_rebate_proportion()))
//                .focus(agent.getFocus())
                .team_number(statistics.getTeam_number())
                .note(agent.getNote())
                .build();
    }
}
