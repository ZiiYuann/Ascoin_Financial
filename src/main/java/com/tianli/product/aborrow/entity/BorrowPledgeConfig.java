package com.tianli.product.aborrow.entity;

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
public class BorrowPledgeConfig {

    private String coin;

    private String logo;

    /**
     * 初始质押
     */
    private BigDecimal initPledgeRate;

    /**
     * 预警质押
     */
    private BigDecimal warnPledgeRate;

    /**
     * 强制质押
     */
    private BigDecimal lqPledgeRate;

    /**
     * assure强制质押
     */
    private BigDecimal assureLqPledgeRate;

    private int weight;

    private int status;

}
