package com.tianli.borrow.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.io.Serializable;
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
public class BorrowPledgeCoinConfigVO implements Serializable {

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
    @BigDecimalFormat("#.##%")
    private BigDecimal initialPledgeRate;

    /**
     * 警告质押率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal warnPledgeRate;

    /**
     * 强平质押率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal liquidationPledgeRate;

}
