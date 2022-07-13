package com.tianli.financial.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.FinancialProductType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class AccrueIncomeLog extends Model<AccrueIncomeLog> {

    @Id
    private Long id;

    private Long uid;

    /**
     * 申购记录id
     */
    private Long recordId;

    /**
     * 产品类型
     */
    private FinancialProductType financialProductType;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 累计收益金额
     */
    private BigDecimal accrueIncomeFee;

    /**
     * 最近更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
