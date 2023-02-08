package com.tianli.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-08
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResultDto {
    private Long id;

    private BigDecimal amount;
}
