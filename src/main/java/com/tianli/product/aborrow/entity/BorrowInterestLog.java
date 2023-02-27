package com.tianli.product.aborrow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.product.aborrow.enums.InterestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowInterestLog {

    @TableId
    private Long id;

    private Long uid;

    private Long bid;

    private InterestType interestType;

    private String coin;

    private BigDecimal amount;

    private LocalDateTime interestTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
