package com.tianli.charge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAdvance {

    private Long id;

    private Long uid;

    private Long productId;

    private String txid;

    private BigDecimal amount;

    private LocalDateTime createTime;
}
