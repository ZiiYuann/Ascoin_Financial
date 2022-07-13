package com.tianli.financial.entity;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.FinancialProductType;
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
public class DailyIncomeLog {

    @Id
    private Long id;

    private Long uid;

    /**
     * 申购记录id
     */
    private Long recordId;

    /**
     * 产品类型
     */
    private FinancialProductType financialProductType;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 收益金额
     */
    private BigDecimal incomeFee;

    /**
     * 记息当天时间，自动化脚本凌晨跑，时间就为前一天零点
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
