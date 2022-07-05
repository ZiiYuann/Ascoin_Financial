package com.tianli.management.agentadmin.vo;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.settlement.mapper.LowSettlement;
import com.tianli.dividends.settlement.mapper.LowSettlementChargeType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SeniorLowSettlementVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 高级代理商id
     */
    private Long senior_uid;

    /**
     * 低级代理商id
     */
    private Long low_uid;

    /**
     * 金额
     */
    private double amount;

    /**
     * 交易类型
     */
    private LowSettlementChargeType charge_type;

    /**
     * 备注
     */
    private String note;

    public static SeniorLowSettlementVO trans(LowSettlement lowSettlement) {
        BigInteger amount;
        if(LowSettlementChargeType.transfer_into.equals(lowSettlement.getCharge_type())){
            amount = lowSettlement.getAmount().negate();
        } else {
            amount = lowSettlement.getAmount();
        }
        return SeniorLowSettlementVO.builder()
                .id(lowSettlement.getId())
                .create_time(lowSettlement.getCreate_time())
                .senior_uid(lowSettlement.getSenior_uid())
                .low_uid(lowSettlement.getLow_uid())
                .amount(TokenCurrencyType.usdt_omni.money(amount))
                .charge_type(lowSettlement.getCharge_type())
                .note(lowSettlement.getNote())
                .build();
    }
}
