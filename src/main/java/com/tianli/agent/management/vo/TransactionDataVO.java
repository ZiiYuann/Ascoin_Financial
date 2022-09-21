package com.tianli.agent.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionDataVO {

    private BigDecimal purchaseAmount;

    private BigDecimal redemptionAmount;

    private BigDecimal interestAmount;

}
