package com.tianli.openapi.entity;

import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRewardRecord {

    private Long id;

    private Long uid;

    private BigDecimal amount;

    private ChargeType type;

    private String coin;

    private LocalDateTime giveTime;

    private Long orderId;

}
