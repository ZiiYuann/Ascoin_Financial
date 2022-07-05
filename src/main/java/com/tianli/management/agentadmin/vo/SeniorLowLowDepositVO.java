package com.tianli.management.agentadmin.vo;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.deposit.mapper.LowDeposit;
import com.tianli.deposit.mapper.LowDepositChargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 低级代理商保证金
 * </p>
 *
 * @author hd
 * @since 2020-12-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeniorLowLowDepositVO {

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
    private LowDepositChargeType charge_type;

    /**
     * 备注
     */
    private String note;

    public static SeniorLowLowDepositVO trans(LowDeposit lowDeposit) {
        BigInteger amount;
        if(LowDepositChargeType.withdraw.equals(lowDeposit.getCharge_type())){
            amount = lowDeposit.getAmount().negate();
        } else {
            amount = lowDeposit.getAmount();
        }
        return SeniorLowLowDepositVO.builder()
                .id(lowDeposit.getId())
                .create_time(lowDeposit.getCreate_time())
                .senior_uid(lowDeposit.getSenior_uid())
                .low_uid(lowDeposit.getLow_uid())
                .amount(TokenCurrencyType.usdt_omni.money(amount))
                .charge_type(lowDeposit.getCharge_type())
                .note(lowDeposit.getNote())
                .build();
    }
}
