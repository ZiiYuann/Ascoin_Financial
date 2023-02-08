package com.tianli.openapi.query;

import com.tianli.charge.enums.ChargeType;
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
public class UserTransferQuery {

    private Long transferUid;

    private Long receiveUid;

    private String coin;

    private BigDecimal amount;

    private Long relatedId;

    private ChargeType chargeType;
}
