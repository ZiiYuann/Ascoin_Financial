package com.tianli.product.afinancial.vo;

import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.PurchaseTerm;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 持有产品信息
 *
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Data
public class MUserHoldRecordDetailsVO {

    private Long uid;

    /**
     * 记录id
     */
    private Long productId;

    /**
     * 记录id
     */
    private Long recordId;

    /**
     * 活期/定期产品
     */
    private ProductType productType;

    /**
     * 产品名称
     */
    private String name;

    /**
     * nameEn
     */
    private String nameEn;

    private BigDecimal holdAmount;

    /**
     * 收益信息
     */
    private IncomeVO incomeVO;

    private String logo;

    private String coin;

    private BigDecimal accrueIncomeAmount;

    private PurchaseTerm term;

    /**
     * 申购时间
     */
    private LocalDateTime purchaseTime;

    /**
     * 记息时间
     */
    private LocalDateTime incomeTime;

    /**
     * 生息天数
     */
    private long incomeDays;


}
