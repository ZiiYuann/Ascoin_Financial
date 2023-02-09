package com.tianli.management.query;

import com.tianli.product.afund.contant.FundIncomeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundIncomeCompensateQuery {


    private Long fundId;

    private LocalDateTime now;

    private BigDecimal amount;

    private Integer status = FundIncomeStatus.calculated;
}
