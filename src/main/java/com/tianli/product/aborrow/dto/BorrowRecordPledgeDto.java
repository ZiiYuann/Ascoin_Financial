package com.tianli.product.aborrow.dto;

import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.afinancial.entity.FinancialRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordPledgeDto {

    private Long id;

    private PledgeType pledgeType;

    private Long uid;

    private String coin;

    private BigDecimal amount;

    private Long recordId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private FinancialRecord financialRecord;
}
