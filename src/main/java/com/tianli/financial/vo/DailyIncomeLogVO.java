package com.tianli.financial.vo;

import com.tianli.common.blockchain.CurrencyCoin;


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
public class DailyIncomeLogVO {

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


}
