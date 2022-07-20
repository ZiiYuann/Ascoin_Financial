package com.tianli.financial.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.entity.FinancialIncomeDaily;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Data
public class FinancialIncomeDailyVO {

    /**
     * 收益金额
     */
    private BigDecimal incomeFee;

    /**
     * 记息当天时间，自动化脚本凌晨跑，时间就为前一天零点
     */
    private LocalDateTime finishTime;

    /**
     * 币别
     */
    private CurrencyCoin coin;

    public static FinancialIncomeDailyVO toVO(FinancialIncomeDaily financialIncomeDaily) {
        FinancialIncomeDailyVO dailyIncomeLogVO = new FinancialIncomeDailyVO();
        dailyIncomeLogVO.setIncomeFee(financialIncomeDaily.getIncomeAmount());
        dailyIncomeLogVO.setFinishTime(financialIncomeDaily.getFinishTime());
        return dailyIncomeLogVO;
    }
}
