package com.tianli.product.afund.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 基金收益记录
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundIncomeRecord extends Model<FundIncomeRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 基金ID
     */
    private Long fundId;
    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 币种
     */
    private String coin;

    /**
     * 年利率
     */
    private BigDecimal rate;

    /**
     * 持有数额
     */
    private BigDecimal holdAmount;

    /**
     * 利息数额
     */
    private BigDecimal interestAmount;

    /**
     * 状态 状态（1-已计算 2-待审核 3-已发放 4-审核失败）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private String orderNo;

}
