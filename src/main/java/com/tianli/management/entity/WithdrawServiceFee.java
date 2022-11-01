package com.tianli.management.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawServiceFee {

    @Id
    private Long id;

    private LocalDate createTime;

    private BigDecimal eth;

    private BigDecimal bnb;

    private BigDecimal trx;
}
