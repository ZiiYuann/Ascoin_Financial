package com.tianli.management.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzy
 * @since  2022/4/1 6:52 下午
 */
@Data
public class FinancialUserListVO {
    /**
     * 现存款总额
     */
    private double totalCurrentDeposit = 0;
    /**
     * 历史存款总额
     */
    private double totalHistoricalDeposits = 0;
    /**
     * 历史赎回总额
     */
    private double totalRedemption = 0;

    private List<FinancialUserRecordListVO> financialUserRecordListVos = new ArrayList<>();
}
