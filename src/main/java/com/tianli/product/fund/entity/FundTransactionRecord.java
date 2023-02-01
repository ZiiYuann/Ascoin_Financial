package com.tianli.product.fund.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;

import com.tianli.product.fund.enums.FundTransactionType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 基金交易记录
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class FundTransactionRecord extends Model<FundTransactionRecord> {

    private static final long serialVersionUID=1L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单ID
     */
    private Long fundId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 交易类型
     */
    private FundTransactionType type;

    /**
     * 币种
     */
    private String coin;

    /**
     * 年化率
     */
    private BigDecimal rate;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 交易数额
     */
    private BigDecimal transactionAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
