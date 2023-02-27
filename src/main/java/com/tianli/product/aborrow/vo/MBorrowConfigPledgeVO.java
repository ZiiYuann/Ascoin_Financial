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
public class MBorrowConfigPledgeVO {

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

    private Integer weight;

    private Integer status;

    public BorrowStatus getBorrowStatus() {
        return BorrowStatus.valueOf(status);
    }

}
