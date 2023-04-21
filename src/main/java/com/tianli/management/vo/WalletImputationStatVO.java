package com.tianli.management.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @autoor xianeng
 * @data 2023/4/20 15:07
 */
@Data
public class WalletImputationStatVO {

    /**
     * 币种
     */
    private String coin;

    /**
     * 待归集数量
     */
    private BigDecimal waitImputationAmount;

    /**
     * 归集地址数量
     */
    private Integer imputationAddressNum;

}
