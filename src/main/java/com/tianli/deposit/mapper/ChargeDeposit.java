package com.tianli.deposit.mapper;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 充值提现表  保证金
 * </p>
 *
 * @author hd
 * @since 2020-12-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeDeposit {

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
    private CurrencyAdaptType currency_type;

    /**
     * 交易类型
     */
    private ChargeDepositType charge_type;

    /**
     * 订单金额
     */
    private BigInteger amount;

    /**
     * 手续费
     */
    private BigInteger fee;

    /**
     * 真实金额
     */
    private BigInteger real_amount;

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

}
