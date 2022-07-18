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
public class DailyIncomeLogVO {

    /**
     * 收益金额
     */
    private BigDecimal incomeFee;

    /**
     * 记息当天时间，自动化脚本凌晨跑，时间就为前一天零点
     */
    private LocalDateTime finishTime;

    public static DailyIncomeLogVO toVO(FinancialIncomeDaily dailyIncomeLog) {
        DailyIncomeLogVO dailyIncomeLogVO = new DailyIncomeLogVO();
        dailyIncomeLogVO.setIncomeFee(dailyIncomeLog.getIncomeAmount());
        dailyIncomeLogVO.setFinishTime(dailyIncomeLog.getFinishTime());
        return dailyIncomeLogVO;
    }
}
