package com.tianli.product.financial.vo;

import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.product.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-02
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecordVO {

    private String name;

    private String nameEn;

    private String detailsId;

    private ChargeType type;

    private ProductType productType;

    private LocalDateTime createTime;

    private ChargeStatus status;

    private BigDecimal amount;

    private String coin;

    private String logo;


}
