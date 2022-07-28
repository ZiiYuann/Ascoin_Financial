package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 质押币种配置
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
@Data
public class BorrowPledgeCoinConfigBO{

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    private Long id;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 初始质押率
     */
    private BigDecimal initialPledgeRate;

    /**
     * 警告质押率
     */
    private BigDecimal warnPledgeRate;

    /**
     * 强平质押率
     */
    private BigDecimal liquidationPledgeRate;

}
