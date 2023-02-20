package com.tianli.product.aborrow.vo;

import com.tianli.charge.enums.ChargeGroup;
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
 * @since 2023-02-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowOperationLogVO {

    private Long uid;

    private String coin;

    private BigDecimal amount;

    private ChargeType chargeType;

    private String chargeTypeNameZn;

    private String chargeTypeNameEn;

    private ChargeGroup chargeGroup;

    private LocalDateTime createTime;

    public String getChargeTypeNameEn() {
        return chargeType.getNameEn();

    }

    public String getChargeTypeNameZn() {
        return chargeType.getNameZn();
    }

    public ChargeGroup getChargeGroup() {
        return ChargeGroup.getInstance(chargeType);
    }
}
