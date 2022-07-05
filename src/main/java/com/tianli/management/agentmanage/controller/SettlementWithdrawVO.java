package com.tianli.management.agentmanage.controller;

import com.tianli.deposit.mapper.DepositSettlementType;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chensong
 * @date 2021-01-11 14:46
 * @since 1.0.0
 */
@Data
@Builder
public class SettlementWithdrawVO {
    private Long id;
    private LocalDateTime create_time;
    private ChargeSettlementStatus status;
    private Long uid;
    private String uid_username;
    private String uid_nick;
    private DepositSettlementType settlement_type;
    private double amount;
    private double fee;
    private double real_amount;
    private String from_address;
    private String to_address;
    private String txid;
    private String note;
    private String review_note;

    public static SettlementWithdrawVO trans(ChargeSettlement settlement){
        return SettlementWithdrawVO.builder()
                .id(settlement.getId())
                .create_time(settlement.getCreate_time())
                .status(settlement.getStatus())
                .uid(settlement.getUid())
                .uid_username(settlement.getUid_username())
                .uid_nick(settlement.getUid_nick())
                .settlement_type(settlement.getSettlement_type())
                .amount(Objects.nonNull(settlement.getAmount())?settlement.getCurrency_type().money(settlement.getAmount()):0)
                .fee(Objects.nonNull(settlement.getFee())?settlement.getCurrency_type().money(settlement.getFee()):0)
                .real_amount(Objects.nonNull(settlement.getReal_amount())?settlement.getCurrency_type().money(settlement.getReal_amount()):0)
                .from_address(settlement.getFrom_address())
                .to_address(settlement.getTo_address())
                .txid(settlement.getTxid())
                .note(settlement.getNote())
                .review_note(settlement.getReview_note()).build();
    }
}
