package com.tianli.management.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Data
@NoArgsConstructor
public class ProductSummaryDataDto {

    private Long productId;

    private BigDecimal useQuota;

    private BigInteger holdUserCount;

}
