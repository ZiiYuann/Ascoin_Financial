package com.tianli.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 *
 * @author wangqiyun
 * @since 2020/3/31 11:26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`order`")
public class Order {

    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;

    /**
     * 交易类型
     */
    private ChargeType type;

    /**
     * 交易状态
     */
    private ChargeStatus status;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 提币金额 （这里不是真是到账金额   真实到账 = 提币金额 - 手续费 = amount - serviceAmount ）
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal serviceAmount = BigDecimal.ZERO;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订单完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 不同类型的交易关联不同的附录表 设计的不合理（我的锅）
     * <p>
     * 红包发送: red_envelope 红包id
     * 抢红包:  red_envelope_spilt_get_record 抢红包记录id
     * 提现 + 充值: order_charge_info 链上记录表
     * 申购: [区分本地申购和普通申购通过 order_no]
     * 本地申购 order_advance id 一般申购（基金：基金持用fund_record id 理财：理财持用 financial_record id）
     * 借贷：弃用
     * 赎回：financial_record id
     *
     */
    private Long relatedId;

    /**
     * 审核信息关联ID
     */
    private Long reviewerId;

}
