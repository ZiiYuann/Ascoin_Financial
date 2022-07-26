package com.tianli.borrow.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 理财产品质押信息
 * </p>
 *
 * @author xianeng
 * @since 2022-07-25
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class FinancialPledgeInfo extends Model<FinancialPledgeInfo> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 理财产品ID
     */
    private Long financialId;

    /**
     * 借币订单ID
     */
    private Long borrowOrderId;

    /**
     * 质押金额
     */
    private BigDecimal pledgeAmount;

    /**
     * 创建时间
     */
    private Date createTime;


}