package com.tianli.openapi.query;

import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-24
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenapiOperationQuery {

    private Long id;

    private Long uid;

    private BigDecimal amount;

    private ChargeType type;

    private String coin;

    private Long give_time;

}
