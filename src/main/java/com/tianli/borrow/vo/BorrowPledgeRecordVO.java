package com.tianli.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 借币质押记录
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Data
public class BorrowPledgeRecordVO implements Serializable {

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime pledgeTime;


}
