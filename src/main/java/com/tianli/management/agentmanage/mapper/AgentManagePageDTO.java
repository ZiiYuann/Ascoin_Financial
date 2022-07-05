package com.tianli.management.agentmanage.mapper;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class AgentManagePageDTO implements Serializable {
    private Long id;
    private String nick;
    private String username;
    private BigInteger balance;
    private BigInteger profit;
    private BigInteger profit_BF;
    private BigInteger unsettlement;
    private BigInteger unsettlementBF;
    private BigInteger remain;
    private boolean focus;
    private LocalDateTime create_time;
    private Double expect_dividends;
    private BigInteger expect_deposit;
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
}
