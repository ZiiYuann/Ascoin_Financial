package com.tianli.management.vo;

import com.tianli.charge.enums.ChargeTypeGroupEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationGroupAmountVO {

    private ChargeTypeGroupEnum operationGroup;

    private BigDecimal fee;

    private List<OperationTypeAmountVO> operationTypeAmountVOS;

    private String operationGroupName;

    public String getOperationGroupName() {
        return operationGroup.getTypeGroup();
    }
}
