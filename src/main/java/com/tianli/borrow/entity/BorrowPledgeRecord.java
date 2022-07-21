package com.tianli.borrow.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;

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
@EqualsAndHashCode(callSuper = false)
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
    private BigDecimal number;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 调整时间
     */
    private Date adjustmentTime;

    /**
     * 创建时间
     */
    private Date createTime;


}
