package com.tianli.management.agentadmin.vo;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.deposit.mapper.DepositSettlementType;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class ChargeSettlementRechargeVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 完成时间
     */
    private LocalDateTime complete_time;

    /**
     * 币种类型
     */
    private TokenCurrencyType currency_type;

    /**
     * 结账方式,链交易or 财务平账
     */
    private DepositSettlementType settlement_type;

    /**
     * 订单金额
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
     * 订单状态
     */
    private ChargeSettlementStatus status;

    /**
     * 交易哈希
     */
    private String txid;

    public static ChargeSettlementRechargeVO trans(ChargeSettlement cs){
        return ChargeSettlementRechargeVO.builder()
                .id(cs.getId())
                .currency_type(cs.getCurrency_type())
                .complete_time(cs.getComplete_time())
                .status(cs.getStatus())
                .amount(cs.getCurrency_type().money(cs.getAmount()))
                .settlement_type(cs.getSettlement_type())
                .from_address(cs.getFrom_address())
                .to_address(cs.getTo_address())
                .txid(cs.getTxid())
                .build();
    }
}