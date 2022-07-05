package com.tianli.management.financial.dto;

import cn.hutool.core.convert.Convert;
import lombok.Data;

import java.math.BigInteger;

/**
 * @author lzy
 * @date 2022/4/1 9:18 下午
 */
@Data
public class FinancialUserTotalDto {

    /**
     * 现存款总额
     */
    private BigInteger totalCurrentDeposit = Convert.toBigInteger(0);
    /**
     * 历史存款总额
     */
    private BigInteger totalHistoricalDeposits = Convert.toBigInteger(0);
    /**
     * 历史赎回总额
     */
    private BigInteger totalRedemption = Convert.toBigInteger(0);
}
