package com.tianli.product.aborrow.entity;

import com.tianli.product.aborrow.enums.BorrowType;
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
public class BorrowRecordSnapshot {

    private Long id;

    private Long bid;

    private BorrowType borrowType;

    private Long uid;

    private String coin;

    private BigDecimal amount;

    private PledgeType pledgeType;

    // 相关的记录质押记录id
    private Long recordId;

    // 快照当时汇率
    private BigDecimal rate;
}
