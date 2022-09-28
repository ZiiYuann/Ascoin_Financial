package com.tianli.charge.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.fund.enums.FundTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-28
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderFundTransactionVO extends OrderBaseVO {
    private static final long serialVersionUID = 1L;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 年化率
     */
    private BigDecimal rate;

    /**
     * id
     */
    private Long id;

    /**
     * 交易数额
     */
    private BigDecimal transactionAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 预计收益
     */
    private BigDecimal expectedIncome;

    /**
     * 状态
     */
    private Integer status;
}
