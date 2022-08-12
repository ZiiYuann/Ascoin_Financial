package com.tianli.borrow.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 借币质押记录
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Data
@Builder
public class BorrowPledgeRecord extends Model<BorrowPledgeRecord> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 币种
     */
    private String coin;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 调整时间
     */
    private LocalDateTime pledgeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
