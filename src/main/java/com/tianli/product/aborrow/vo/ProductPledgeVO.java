package com.tianli.product.aborrow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPledgeVO {
    /**
     * 主键
     */
    @Id
    private Long id;

    private String productName;

    private String productNameEn;

    private String logo;

    private String coin;

    private BigDecimal holdAmount;

    /**
     * 是否被质押
     */
    private boolean pledge;
}
