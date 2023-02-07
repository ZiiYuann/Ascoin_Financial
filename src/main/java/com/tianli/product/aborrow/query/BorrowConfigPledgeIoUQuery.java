package com.tianli.product.aborrow.query;

import com.tianli.common.query.IoUQuery;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@EqualsAndHashCode(callSuper = true)
public class BorrowConfigPledgeIoUQuery extends IoUQuery {

    @NotBlank
    private String coin;

    @NotBlank
    private String logo;

    /**
     * 初始质押
     */
    @NotNull
    private BigDecimal initPledgeRate;

    /**
     * 预警质押
     */
    @NotNull
    private BigDecimal warnPledgeRate;

    /**
     * 强制质押
     */
    @NotNull
    private BigDecimal lqPledgeRate;

    /**
     * assure强制质押
     */
    @NotNull
    private BigDecimal assureLqPledgeRate;

    @NotNull
    private Integer weight;

}
