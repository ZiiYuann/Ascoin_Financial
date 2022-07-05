package com.tianli.management.agentmanage.controller;

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
 * @date 2020-12-31 16:59
 * @since 1.0.0
 */
@Data
@Builder
public class DepositWithdrawVO {

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
    private ChargeDepositStatus status;

    /**
     * 所属用户id
     */
    private Long uid;

    /**
     * 订单号
     */
    private String sn;

    /**
     * 用户账号
     */
    private String uid_username;

    /**
     * 用户昵称
     */
    private String uid_nick;

    /**
     * 结账方式,链交易or 财务平账
     */
    private DepositSettlementType settlement_type;

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

    public static DepositWithdrawVO trans(ChargeDeposit chargeDeposit){
        BigInteger realAmount = BigInteger.ZERO;
        if(Objects.equals(ChargeDepositStatus.transaction_success,chargeDeposit.getStatus())){
            realAmount = chargeDeposit.getReal_amount();
        }
        DepositWithdrawVO vo = DepositWithdrawVO.builder()
                .id(chargeDeposit.getId()).create_time(chargeDeposit.getCreate_time())
                .status(chargeDeposit.getStatus()).uid(chargeDeposit.getUid())
                .uid_username(chargeDeposit.getUid_username()).uid_nick(chargeDeposit.getUid_nick())
                .sn(chargeDeposit.getSn())
                .settlement_type(chargeDeposit.getSettlement_type())
                .amount(chargeDeposit.getCurrency_type().money(chargeDeposit.getAmount()))
                .fee(chargeDeposit.getCurrency_type().money(chargeDeposit.getFee()))
                .real_amount(chargeDeposit.getCurrency_type().money(realAmount))
                .from_address(chargeDeposit.getFrom_address()).to_address(chargeDeposit.getTo_address())
                .txid(chargeDeposit.getTxid()).note(chargeDeposit.getNote()).review_note(chargeDeposit.getReview_note())
                .build();
        return vo;
    }
}
