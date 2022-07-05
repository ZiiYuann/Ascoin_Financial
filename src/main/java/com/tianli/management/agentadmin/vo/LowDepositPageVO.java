package com.tianli.management.agentadmin.vo;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.deposit.mapper.LowDeposit;
import com.tianli.deposit.mapper.LowDepositChargeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-01-06 10:48
 * @since 1.0.0
 */
@Data
@Builder
public class LowDepositPageVO {
    private Long id;
    private double amount;
    private LowDepositChargeType charge_type;
    private LocalDateTime create_time;
    private String note;

    public static LowDepositPageVO trans(LowDeposit deposit){
        BigInteger amount;
        if(LowDepositChargeType.withdraw.equals(deposit.getCharge_type())){
            amount = deposit.getAmount().negate();
        } else {
            amount = deposit.getAmount();
        }
        return LowDepositPageVO.builder()
                .id(deposit.getId())
                .amount(TokenCurrencyType.usdt_omni.money(amount))
                .charge_type(deposit.getCharge_type())
                .create_time(deposit.getCreate_time())
                .note(deposit.getNote()).build();
    }
}
