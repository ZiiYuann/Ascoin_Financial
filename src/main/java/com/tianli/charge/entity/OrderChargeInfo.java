package com.tianli.charge.entity;

import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 如果订单为充值或者提现，此为链上交易信息表
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderChargeInfo{

    /**
     * 主键ID
     */
    private Long id;

    private Long uid;

    /**
     * 格式化之后的费用
     */
    private BigDecimal fee;

    /**
     * 手续费
     */
    private BigDecimal serviceFee;

    /**
     * 真正转账的费用，且没有进行过格式化（提币的时候 为 提现金额 - 手续费）
     */
    private BigInteger realFee;

    /**
     *
     * 矿工费
     */
    private BigDecimal minerFee;

    /**
     * 交易hash
     */
    private String txid;

    private String fromAddress;

    private String toAddress;

    private LocalDateTime createTime;

    private String coin;

    private NetworkType network;

}
