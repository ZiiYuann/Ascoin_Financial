package com.tianli.management.agentmanage.controller;

import com.tianli.common.DoubleDecimalTrans;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.agentmanage.mapper.AgentManagePageDTO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class AgentManagePageVO implements Serializable {
    private Long id;
    private String nick;
    private String username;
    private double balance;
    private double profit;
    private double profit_BF;
    private double remain;
    /**
     * 未结算数额
     */
    private double unSettlement;

    private double unSettlementBF;
    private boolean focus;
    private double settled_number;
    private double settled_number_BF;
    private LocalDateTime create_time;
    private Double expect_deposit;
    private Double expect_dividends;
    private Double real_dividends;
    private Double steady_dividends;
    private Double normal_rebate_proportion;
    private Double steady_rebate_proportion;
    private Integer team_number;
    private Integer referral_number;
    private String deposit_omni;
    private String deposit_erc20;
    private String deposit_trc20;
    private String settlement_omni;
    private String settlement_erc20;
    private String settlement_trc20;
    private String settlement_bep20;
    private String note;

    public static AgentManagePageVO trans(AgentManagePageDTO dto) {
        AgentManagePageVO vo = new AgentManagePageVO();
        BeanUtils.copyProperties(dto, vo, "balance", "profit", "settled_number", "remain", "expect_deposit",
                "expect_dividends", "real_dividends", "steady_dividends", "normal_rebate_proportion", "steady_rebate_proportion");
        BigInteger balance = dto.getBalance();
        BigInteger profit = dto.getProfit();
        BigInteger profit_BF = dto.getProfit_BF();
        BigInteger remain = dto.getRemain();
        BigInteger unsettlement = dto.getUnsettlement();
        BigInteger unsettlement_BF = dto.getUnsettlementBF();
        vo.setBalance(Objects.nonNull(balance) ? TokenCurrencyType.usdt_omni.money(balance) : 0);
        vo.setProfit(Objects.nonNull(profit) ? TokenCurrencyType.usdt_omni.money(profit) : 0);
        vo.setProfit_BF(Objects.nonNull(profit_BF) ? CurrencyTokenEnum.BF_bep20.money(profit_BF) : 0);
        vo.setUnSettlement(Objects.nonNull(unsettlement) ? TokenCurrencyType.usdt_omni.money(unsettlement) : 0);
        vo.setUnSettlementBF(Objects.nonNull(unsettlement_BF) ? CurrencyTokenEnum.BF_bep20.money(unsettlement_BF) : 0);
        vo.setRemain(Objects.nonNull(remain) ? TokenCurrencyType.usdt_omni.money(remain) : 0);
        vo.setSettled_number(Double.valueOf(String.valueOf(new BigDecimal(String.valueOf(vo.getProfit())).subtract(new BigDecimal(String.valueOf(vo.getUnSettlement()))))));
        vo.setSettled_number_BF(Double.valueOf(String.valueOf(new BigDecimal(String.valueOf(vo.getProfit_BF())).subtract(new BigDecimal(String.valueOf(vo.getUnSettlementBF()))))));
        vo.setExpect_deposit(Objects.nonNull(dto.getExpect_deposit()) ? TokenCurrencyType.usdt_omni.money(dto.getExpect_deposit()) : 0);
        vo.setExpect_dividends(Objects.nonNull(dto.getExpect_dividends()) ? DoubleDecimalTrans.double_multiply_hundred(dto.getExpect_dividends()) : 0);
        vo.setReal_dividends(Objects.nonNull(dto.getReal_dividends()) ? DoubleDecimalTrans.double_multiply_hundred(dto.getReal_dividends()) : 0);
        vo.setSteady_dividends(Objects.nonNull(dto.getSteady_dividends()) ? DoubleDecimalTrans.double_multiply_hundred(dto.getSteady_dividends()) : 0);
        vo.setNormal_rebate_proportion(Objects.nonNull(dto.getNormal_rebate_proportion()) ? DoubleDecimalTrans.double_multiply_hundred(dto.getNormal_rebate_proportion()) : 0);
        vo.setSteady_rebate_proportion(Objects.nonNull(dto.getSteady_rebate_proportion()) ? DoubleDecimalTrans.double_multiply_hundred(dto.getSteady_rebate_proportion()) : 0);
        return vo;
    }
}
