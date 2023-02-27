package com.tianli.product.aborrow.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2023-02-15
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MBorrowOperationLogVO {

    private Long id;

    private Long uid;

    private String coin;

    private BigDecimal amount;

    private ChargeType chargeType;

    private String chargeTypeName;

    private BigDecimal prePledgeRate;

    private BigDecimal afterPledgeRate;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public String getChargeTypeName() {
        return chargeType.getNameZn();
    }
}
