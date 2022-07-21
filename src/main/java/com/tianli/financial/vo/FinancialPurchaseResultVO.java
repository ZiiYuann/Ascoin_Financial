package com.tianli.financial.vo;

import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.ProductType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-12
 **/
@Data
public class FinancialPurchaseResultVO {

    private String name;
    private String nameEn;

    /**
     * 主键
     */
    private Long id;

    /**
     * 理财产品id
     */
    private Long financialProductId;

    /**
     * 活期/定期产品
     */
    private ProductType financialProductType;

    /**
     * 理财本金
     */
    private BigDecimal amount;

    /**
     * 参考年化
     */
    private BigDecimal rate;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 状态
     */
    private RecordStatus status;

    /**
     * 状态描述
     */
    private String statusDes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
