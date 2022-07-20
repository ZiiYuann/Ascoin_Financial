package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-19
 **/
@Data
@Builder
public class FinancialWalletBoardSummaryVO {

    /**
     * 激活钱包总人数
     */
    private BigInteger totalActiveWalletCount;

    /**
     * 日期内激活钱包人数
     */
    private BigInteger newActiveWalletCount;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;

    /**
     * 提币金额
     */
    private BigDecimal withdrawAmount;

    /**
     * 日期详情数据
     */
    private List<FinancialWalletBoardVO> data;
}
