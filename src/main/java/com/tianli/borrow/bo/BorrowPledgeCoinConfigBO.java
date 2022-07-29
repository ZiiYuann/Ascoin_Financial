package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

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
     * 预警质押率
     */
    private BigDecimal warnPledgeRate;

    /**
     * 强平质押率
     */
    private BigDecimal liquidationPledgeRate;

    public void convertToRate(){
        if(Objects.nonNull(initialPledgeRate))this.setInitialPledgeRate(initialPledgeRate.divide(BigDecimal.valueOf(100) ,8, RoundingMode.UP));
        if(Objects.nonNull(warnPledgeRate))this.setWarnPledgeRate(warnPledgeRate.divide(BigDecimal.valueOf(100) ,8, RoundingMode.UP));
        if(Objects.nonNull(liquidationPledgeRate))this.setLiquidationPledgeRate(liquidationPledgeRate.divide(BigDecimal.valueOf(100) ,8, RoundingMode.UP));
    }

}
