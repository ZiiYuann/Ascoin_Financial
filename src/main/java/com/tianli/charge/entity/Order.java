package com.tianli.charge.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * 订单表
 * @author  wangqiyun
 * @since  2020/3/31 11:26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value ="`order`")
public class Order {
    /**
     * 订单号
     */
    @TableId
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
     * 真实等金额 提币 = amount - serviceAmount，充值 = amount - serviceAmount
     */
    private BigDecimal amount;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订单完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 不同类型的交易关联不同的附录表
     *
     */
    private Long relatedId;

    /**
     * 审核信息关联ID
     */
    private Long reviewerId;

}
