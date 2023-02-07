package com.tianli.product.aborrow.vo;

import com.tianli.product.aborrow.enums.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MBorrowConfigCoinVO {

    private String coin;

    private String logo;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal hourRate;

    private Integer weight;

    private Integer status;

    private BorrowStatus borrowStatus;

    public BorrowStatus getBorrowStatus() {
        return BorrowStatus.valueOf(status);
    }
}
