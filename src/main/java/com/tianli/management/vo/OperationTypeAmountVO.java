package com.tianli.management.vo;

import com.tianli.charge.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationTypeAmountVO {

    private OperationTypeEnum operationType;

    private String operationTypeName;

    private BigDecimal fee;

    public String getOperationTypeName() {
        return operationType.getName();
    }
}
