package com.tianli.product.aborrow.vo;

import com.tianli.product.aborrow.enums.PledgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-15
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MBorrowRecordVO {

    private Long brId;

    private String coin;

    private BigDecimal amount;

    private PledgeType pledgeType;

    private BigDecimal rate;

}
