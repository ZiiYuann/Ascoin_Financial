package com.tianli.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiquidateFillResponse {
    // 交易的价格
    private BigDecimal price;
    // 交易的数量
    private BigDecimal qty;
    // 手续费金额
    private BigDecimal commission;
    // 手续费的币种
    private String commissionAsset;
    // 交易ID
    private int tradeId;
}
