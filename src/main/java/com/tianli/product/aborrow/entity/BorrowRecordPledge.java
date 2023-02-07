package com.tianli.product.aborrow.entity;

import com.tianli.product.aborrow.enums.PledgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordPledge {

    private Long id;

    private Long bid;

    private Long uid;

    private String coin;

    private BigDecimal amount;

    private PledgeType type;

    // 相关的记录质押记录id
    private Long recordId;
}
