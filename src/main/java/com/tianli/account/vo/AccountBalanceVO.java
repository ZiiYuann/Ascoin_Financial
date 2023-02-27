package com.tianli.account.vo;

import com.tianli.chain.entity.CoinBase;
import com.tianli.common.annotation.BigDecimalFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceVO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 币种
     */
    private String coin;

    /**
     * logo 地址
     */
    private String logo;

    /**
     * 转化成为美元的汇率
     */
    private BigDecimal dollarRate;

    /**
     * 总资产
     */
    private BigDecimal assets;

    /**
     * 总资产
     */
    @BigDecimalFormat("0.00")
    private BigDecimal dollarAssets;

    /**
     * 总余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal freeze;

    private BigDecimal pledgeFreeze;

    /**
     * 剩余余额
     */
    private BigDecimal remain;

    /**
     * 理财持有
     */
    private BigDecimal holdAmount;

    /**
     * 总余额 美元
     */
    @BigDecimalFormat("0.00")
    private BigDecimal dollarBalance;

    /**
     * 冻结余额 美元
     */
    @BigDecimalFormat("0.00")
    private BigDecimal dollarFreeze;

    /**
     * 剩余余额 美元
     */
    @BigDecimalFormat("0.00")
    private BigDecimal dollarRemain;

    @BigDecimalFormat("0.00")
    private BigDecimal dollarPledgeFreeze;

    private int weight;


    public static AccountBalanceVO getDefault(CoinBase coin) {
        return AccountBalanceVO.builder()
                .id(-1L)
                .coin(coin.getName())
                .balance(BigDecimal.ZERO)
                .logo(coin.getLogo())
                .dollarRate(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .freeze(BigDecimal.ZERO)
                .remain(BigDecimal.ZERO)
                .dollarBalance(BigDecimal.ZERO)
                .dollarFreeze(BigDecimal.ZERO)
                .assets(BigDecimal.ZERO)
                .dollarAssets(BigDecimal.ZERO)
                .dollarRemain(BigDecimal.ZERO).build();

    }


}
