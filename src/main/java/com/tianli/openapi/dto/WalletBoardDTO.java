package com.tianli.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBoardDTO {
    // IN
    // 提现手续费
    private BigDecimal withdrawServiceFee;

    // 基金收益
    private BigDecimal fundIncomeFee;

    // OUT
    // 理财收益
    private BigDecimal financialIncomeFee;

    // 归集 + 转账矿工费
    private BigDecimal recycleServiceFee;

    // WORK
    // 钱包当前余额
    private BigDecimal balanceFee;

    // 总平台提现
    private BigDecimal platformWithdrawFee;

    // 总平台充值
    private BigDecimal platformRechargeFee;

    // 总转入
    private BigDecimal inFee;

    // 总转出
    private BigDecimal outFee;

    // 总用户提现
    private BigDecimal withdrawFee;

    // 总用户充值
    private BigDecimal rechargeFee;

    // USER_WALLET
    // 用户当前余额
    private BigDecimal userBalanceFee;

    // USER_FINANCIAL
    // 理财余额
    private BigDecimal holdFee;

}
