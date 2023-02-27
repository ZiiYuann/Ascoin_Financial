package com.tianli.product.aborrow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tianli.product.aborrow.enums.PledgeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    private Long id;

    private Long uid;

    private BigDecimal currencyPledgeRate;

    private BigDecimal lqPledgeRate;

    private BigDecimal warnPledgeRate;

    private BigDecimal assureLqPledgeRate;

    private boolean autoReplenishment;

    private String borrowCoins;

    private String pledgeCoins;

    private BigDecimal borrowFee;

    private BigDecimal pledgeFee;

    private BigDecimal interestFee;

    private Long newestSnapshotId;

    private boolean finish;

    private PledgeStatus pledgeStatus;

    // 借款时间
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 结束时间
    private LocalDateTime finishTime;
}
