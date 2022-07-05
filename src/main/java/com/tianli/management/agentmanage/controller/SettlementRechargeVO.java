package com.tianli.management.agentmanage.controller;

import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chensong
 * @date 2021-01-11 14:40
 * @since 1.0.0
 */
@Data
@Builder
public class SettlementRechargeVO {
    private LocalDateTime create_time;
    private Long uid;
    private String uid_username;
    private String uid_nick;
    private double amount;
    private String from_address;
    private String to_address;
    private String txid;
    public static SettlementRechargeVO trans(ChargeSettlement settlement){
        return SettlementRechargeVO.builder()
                .create_time(settlement.getCreate_time())
                .uid(settlement.getUid())
                .uid_username(settlement.getUid_username())
                .uid_nick(settlement.getUid_nick())
                .amount(Objects.nonNull(settlement.getAmount()) ? settlement.getCurrency_type().money(settlement.getAmount()):0)
                .from_address(settlement.getFrom_address())
                .to_address(settlement.getTo_address())
                .txid(settlement.getTxid()).build();
    }
}
