package com.tianli.management.agentadmin.vo;

import com.tianli.deposit.mapper.ChargeDeposit;
import com.tianli.deposit.mapper.ChargeDepositStatus;
import com.tianli.deposit.mapper.DepositSettlementType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chensong
 * @date 2021-01-06 11:21
 * @since 1.0.0
 */
@Data
@Builder
public class ChargeDepositWithdrawPageVO {
    private Long id;
    private String txid;
    private ChargeDepositStatus status;
    private DepositSettlementType settlement_type;
    private double amount;
    private double fee;
    private double real_amount;
    private String from_address;
    private String to_address;
    private LocalDateTime create_time;
    private String note;
    private String review_note;

    public static ChargeDepositWithdrawPageVO trans(ChargeDeposit chargeDeposit){
        BigInteger real_amount = BigInteger.ZERO;
        if(ChargeDepositStatus.transaction_success.equals(chargeDeposit.getStatus())){
            real_amount = chargeDeposit.getReal_amount();
        }
        return ChargeDepositWithdrawPageVO.builder()
                .id(chargeDeposit.getId()).txid(chargeDeposit.getTxid())
                .status(chargeDeposit.getStatus()).settlement_type(chargeDeposit.getSettlement_type())
                .amount(Objects.nonNull(chargeDeposit.getAmount())? chargeDeposit.getCurrency_type().money(chargeDeposit.getAmount()):0)
                .fee(Objects.nonNull(chargeDeposit.getFee())? chargeDeposit.getCurrency_type().money(chargeDeposit.getFee()):0)
                .real_amount(chargeDeposit.getCurrency_type().money(real_amount))
                .from_address(chargeDeposit.getFrom_address()).to_address(chargeDeposit.getTo_address())
                .create_time(chargeDeposit.getCreate_time()).note(chargeDeposit.getNote())
                .review_note(chargeDeposit.getReview_note()).build();
    }
}
