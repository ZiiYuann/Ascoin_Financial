package com.tianli.product.aborrow.vo;

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
public class MBorrowHedgeEntrustVO {

    private Long id;

    private Long bid;

    private Long brId;

    private String coin;

    private HedgeType hedgeType;

    private HedgeStatus hedgeStatus;

    private BigDecimal amount;

    // 创建汇率 接管价
    private BigDecimal createRate;

    // 委托汇率
    private BigDecimal entrustRate;

    // 成交汇率
    private BigDecimal translateRate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

}
