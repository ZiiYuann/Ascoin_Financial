package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-16
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAmountDetailsVO {

    /**
     * 账户余额
     */
    private BigDecimal dollarBalance;

    private List<OperationGroupAmountVO> operationGroupAmountVOS;

}
