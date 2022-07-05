package com.tianli.management.agentadmin.vo;

import com.tianli.deposit.mapper.ChargeDeposit;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chensong
 * @date 2021-01-06 11:15
 * @since 1.0.0
 */
@Data
@Builder
public class ChargeDepositRechargePageVO {
    private Long id;
    private String txid;
    private double amount;
    private String from_address;
    private String to_address;
    private LocalDateTime create_time;

    public static ChargeDepositRechargePageVO trans(ChargeDeposit deposit){
        return ChargeDepositRechargePageVO.builder()
                .id(deposit.getId()).txid(deposit.getTxid())
                .amount(Objects.nonNull(deposit.getAmount())? deposit.getCurrency_type().money(deposit.getAmount()):0)
                .from_address(deposit.getFrom_address()).to_address(deposit.getTo_address())
                .create_time(deposit.getCreate_time()).build();
    }
}
