package com.tianli.account.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.account.enums.AccountOperationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 余额变动记录表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AccountBalanceOperationLog extends Model<AccountBalanceOperationLog> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 关联的余额id
     */
    private Long accountBalanceId;

    /**
     * 余额变动类型
     */
    private ChargeType chargeType;

    /**
     * 记录类型
     */
    private AccountOperationType logType;

    /**
     * 币种类型
     */
    private CurrencyCoin coin;

    private NetworkType network;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 总余额	
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal freeze;

    /**
     * 剩余余额
     */
    private BigDecimal remain;

    /**
     * 余额变动描述
     */
    private String des;
}
