package com.tianli.financial.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote 理财日收益表
 * @since 2022-07-13
 **/
@Data
public class FinancialIncomeDaily {

    @Id
    private Long id;

    private Long uid;

    /**
     * 申购记录id
     */
    private Long recordId;

    /**
     * 收益金额
     */
    private BigDecimal incomeAmount;

    /**
     * 记息当天时间，自动化脚本凌晨跑，时间就为前一天零点
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
