package com.tianli.product.aborrow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.product.aborrow.enums.HedgeStatus;
import com.tianli.product.aborrow.enums.HedgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowHedgeEntrust{

    @TableId
    private Long id;

    private Long bid;

    private Long brId;

    private String coin;

    private String hedgeCoin;

    private HedgeType hedgeType;

    private HedgeStatus hedgeStatus;

    private BigDecimal amount;

    private BigDecimal translateAmount;

    // 创建汇率 接管价
    private BigDecimal createRate;

    // 委托汇率
    private BigDecimal entrustRate;

    // 成交汇率
    private BigDecimal translateRate;

    private String liquidateId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

}
