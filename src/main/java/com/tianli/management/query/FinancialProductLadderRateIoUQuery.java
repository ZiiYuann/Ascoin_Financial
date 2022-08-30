package com.tianli.management.query;

import com.tianli.common.query.IoUQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinancialProductLadderRateIoUQuery extends IoUQuery {

    private Long id;

    private Long productId;

    private BigDecimal startPoint;

    private BigDecimal endPoint;

    private BigDecimal rate;
}
