package com.tianli.management.platformfinance;

import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * <p>
 *     平台财务展板 每日明细
 * </p>
 * @author chensong
 *  2020-12-21 16:53
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceExhibitionDetailVO {

    private LocalDate createTime;

    /**
     * 总手续费数额
     */
    private Double feeTotal;

    /**
     * 用户数额提现手续费(USDT)
     */
    private Double withdrawalFee;

    /**
     * 代理商分红结算手续费(USDT)
     */
    private Double settlementFee;

    /**
     * 代理商撤回保证金手续费(USDT)
     */
    private Double depositFee;

    public static FinanceExhibitionDetailVO trans(FinanceExhibitionDetailDTO dto){
        BigInteger withdrawal_fee = dto.getWithdrawal_fee_omni().add(dto.getWithdrawal_fee_erc20().multiply(new BigInteger("100")));
        BigInteger settlement_fee = dto.getSettlement_omni_fee().add(dto.getSettlement_erc20_fee().multiply(new BigInteger("100")));
        BigInteger deposit_fee = dto.getDeposit_omni_fee().add(dto.getDeposit_erc20_fee().multiply(new BigInteger("100")));
        BigInteger fee_total = withdrawal_fee.add(settlement_fee).add(deposit_fee);
        return FinanceExhibitionDetailVO.builder()
                .createTime(dto.getDate())
                .withdrawalFee(TokenCurrencyType.usdt_omni.money(withdrawal_fee))
                .settlementFee(TokenCurrencyType.usdt_omni.money(settlement_fee))
                .depositFee(TokenCurrencyType.usdt_omni.money(deposit_fee))
                .feeTotal(TokenCurrencyType.usdt_omni.money(fee_total)).build();
    }
}
