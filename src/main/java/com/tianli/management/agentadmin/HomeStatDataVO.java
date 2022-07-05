package com.tianli.management.agentadmin;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.agentadmin.mapper.HomeStatDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatDataVO implements Serializable {

    /**
     * 总余额
     */
    private double balance;

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
    private double un_settled_number;

    /**
     * 团队人数
     */
    private long team_number;

    /**
     * 总返佣金额
     */
    private double rebate;
    private double rebateBF;

    public static HomeStatDataVO trans(HomeStatDataDTO dto) {
        return HomeStatDataVO.builder()
                .balance(TokenCurrencyType.usdt_omni.money(dto.getBalance()))
                .profit(TokenCurrencyType.usdt_omni.money(dto.getProfit()))
                .settled_number(TokenCurrencyType.usdt_omni.money(dto.getProfit().subtract(dto.getUn_settled_number())))
                .un_settled_number(TokenCurrencyType.usdt_omni.money(dto.getUn_settled_number()))
                .team_number(dto.getTeam_number())
                .rebate(TokenCurrencyType.usdt_omni.money(dto.getRebate()))
                .build();
    }

}
