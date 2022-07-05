package com.tianli.management.agentadmin.mapper;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@Builder
public class HomeStatDataDTO implements Serializable {
    /**
     * 总余额
     */
    private BigInteger balance;

    /**
     * 代理商总净盈亏
     */
    private BigInteger profit;

    /**
     * 已结算数额
     */
    private BigInteger un_settled_number;

    /**
     * 团队人数
     */
    private Long team_number;

    /**
     * 总返佣金额
     */
    private BigInteger rebate;

}
