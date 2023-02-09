package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 云钱包用户管理上方数据
 *
 * @author chenb
 * @apiNote
 * @since 2023-02-08
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MWalletUserManagerDataVO {

    private BigDecimal rechargeFee;

    private BigDecimal withdrawFee;

    private BigDecimal financialIncomeFee;

    private BigDecimal fundIncomeFee;
}
