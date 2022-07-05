package com.tianli.management.agentmanage.controller;

import com.tianli.deposit.mapper.ChargeDeposit;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2020-12-31 17:39
 * @since 1.0.0
 */
@Data
@Builder
public class DepositRechargeVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 完成时间
     */
    private LocalDateTime complete_time;

    /**
     * 所属用户id
     */
    private Long uid;

    /**
     * 用户账号
     */
    private String uid_username;

    /**
     * 用户昵称
     */
    private String uid_nick;

    /**
     * 缴纳数额
     */
    private double amount;

    /**
     * 发送地址
     */
    private String from_address;

    /**
     * 接受地址
     */
    private String to_address;

    /**
     * 交易哈希
     */
    private String txid;

    public static DepositRechargeVO trans(ChargeDeposit chargeDeposit){
        return DepositRechargeVO.builder()
                .id(chargeDeposit.getId()).create_time(chargeDeposit.getCreate_time())
                .uid(chargeDeposit.getUid()).uid_username(chargeDeposit.getUid_username())
                .uid_nick(chargeDeposit.getUid_nick())
                .amount(chargeDeposit.getCurrency_type().money(chargeDeposit.getAmount()))
                .from_address(chargeDeposit.getFrom_address()).to_address(chargeDeposit.getTo_address())
                .txid(chargeDeposit.getTxid())
                .build();
    }

}
