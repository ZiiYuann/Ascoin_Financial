package com.tianli.product.aborrow.vo;

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
public class MBorrowUserVO {

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

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

}
