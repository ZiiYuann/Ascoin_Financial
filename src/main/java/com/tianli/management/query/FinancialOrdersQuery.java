package com.tianli.management.query;

import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialOrdersQuery {

    private String name;
    private String orderNo;
    private String uid;
    private ProductType productType;
    private CurrencyCoin coin;
    private ChargeStatus status;
    private ChargeType chargeType;

    private List<ChargeType> defaultChargeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
