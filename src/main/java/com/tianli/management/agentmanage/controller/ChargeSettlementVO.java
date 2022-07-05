package com.tianli.management.agentmanage.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.deposit.mapper.DepositSettlementType;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import com.tianli.dividends.settlement.mapper.ChargeSettlementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeSettlementVO implements Serializable {

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
     * 订单状态
     */
    private ChargeSettlementStatus status;

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
     * 头像
     */
    private String uid_avatar;

    /**
     * 订单号
     */
    private String sn;

    /**
     * 币种类型
     */
    private TokenCurrencyType currency_type;

    /**
     * 交易类型
     */
    private ChargeSettlementType charge_type;

    /**
     * 结账方式,链交易or 财务平账
     */
    private DepositSettlementType settlement_type;

    private CurrencyTokenEnum token;

    /**
     * 订单金额
     */
    private double amount;

    /**
     * 手续费
     */
    private double fee;

    /**
     * 真实金额
     */
    private double real_amount;

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

    /**
     * 备注
     */
    private String note;

    /**
     * 审核备注
     */
    private String review_note;

    public static ChargeSettlementVO trans(ChargeSettlement cs) {
        BigInteger realAmount = BigInteger.ZERO;
        if(Objects.equals(ChargeSettlementStatus.transaction_success,cs.getStatus())){
            realAmount = cs.getReal_amount();
        }
        ChargeSettlementVO build = ChargeSettlementVO.builder().build();
        BeanUtils.copyProperties(cs, build);
        build.setAmount(cs.getCurrency_type().money(cs.getAmount()));
        build.setFee(cs.getCurrency_type().money(cs.getFee()));
        build.setReal_amount(cs.getCurrency_type().money(realAmount));
        return build;
    }
}
