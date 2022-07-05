package com.tianli.management.agentadmin.vo;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.settlement.mapper.LowSettlement;
import com.tianli.dividends.settlement.mapper.LowSettlementChargeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-01-06 11:00
 * @since 1.0.0
 */
@Data
@Builder
public class LowSettlementPageVO {
    private Long id;
    private LocalDateTime create_time;
    private double amount;
    private LowSettlementChargeType charge_type;
    private String note;

    public static LowSettlementPageVO trans(LowSettlement settlement){
        BigInteger amount;
        if(LowSettlementChargeType.transfer_into.equals(settlement.getCharge_type())){
            amount =settlement.getAmount().negate();
        } else {
            amount = settlement.getAmount();
        }
        return LowSettlementPageVO.builder().id(settlement.getId())
                .amount(TokenCurrencyType.usdt_omni.money(amount))
                .create_time(settlement.getCreate_time())
                .charge_type(settlement.getCharge_type())
                .note(settlement.getNote()).build();
    }
}
