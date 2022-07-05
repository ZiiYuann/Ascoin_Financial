package com.tianli.management.user.controller;

import cn.hutool.core.convert.Convert;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/12 2:36 下午
 */
@Builder
@Data
public class CustomerProfitDetailsVo {

    /**
     * 充值金额
     */
    private double recharge_amount;
    /**
     * 提现金额
     */
    private double withdrawal_amount;
    /**
     * 利润
     */
    private double profit;

    public static CustomerProfitDetailsVo get(List<Charge> chargeList) {
        BigDecimal recharge_amount = BigDecimal.ZERO;
        BigDecimal withdrawal_amount = BigDecimal.ZERO;
        for (Charge charge : chargeList) {
            TokenCurrencyType tokenCurrencyType = TokenCurrencyType.getTokenCurrencyType(charge.getToken().name());
            if (charge.getCharge_type().equals(ChargeType.recharge)) {
                recharge_amount = recharge_amount.add(Convert.toBigDecimal(tokenCurrencyType.money(charge.getAmount())));
            } else {
                withdrawal_amount = withdrawal_amount.add(Convert.toBigDecimal(tokenCurrencyType.money(charge.getAmount())));
            }
        }
        BigDecimal profit = recharge_amount.subtract(withdrawal_amount);
        return CustomerProfitDetailsVo.builder()
                .recharge_amount(Convert.toDouble(recharge_amount))
                .withdrawal_amount(Convert.toDouble(withdrawal_amount))
                .profit(Convert.toDouble(profit)).build();
    }
}
