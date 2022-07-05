package com.tianli.management.spot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/24 15:38
 */
@Data
public class SGRechargeByTypeVo {
    /**
     * 币种类型
     */
    private String currencyType;
    /**
     * 币类型
     */
    private String token;
    /**
     * 总金额
     */
    private BigDecimal sumAmount;

}
