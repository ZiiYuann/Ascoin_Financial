package com.tianli.dividends.settlement.mapper;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.deposit.mapper.DepositSettlementType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 充值提现表
 * </p>
 *
 * @author hd
 * @since 2020-12-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class ChargeSettlement {

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

    private BigInteger miner_fee;
    private TokenCurrencyType miner_fee_type;
    private CurrencyTokenEnum token;
}
