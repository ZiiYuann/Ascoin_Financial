package com.tianli.product.aborrow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.charge.enums.ChargeType;
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
public class BorrowOperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long uid;

    private Long bid;

    private String coin;

    private BigDecimal amount;

    private ChargeType chargeType;

    private boolean display;

    private BigDecimal prePledgeRate;

    private BigDecimal afterPledgeRate;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public static BorrowOperationLog log(ChargeType chargeType, Long bid, Long uid, String coin, BigDecimal amount) {
        return BorrowOperationLog.builder()
                .bid(bid)
                .uid(uid)
                .coin(coin)
                .amount(amount).chargeType(chargeType).build();
    }

}
