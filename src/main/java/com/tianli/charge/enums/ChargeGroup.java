package com.tianli.charge.enums;

import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
public enum ChargeGroup {
    // 收入
    receive(List.of(ChargeType.recharge, ChargeType.income, ChargeType.redeem, ChargeType.settle
            , ChargeType.borrow, ChargeType.release, ChargeType.agent_fund_sale, ChargeType.fund_redeem
            , ChargeType.fund_interest, ChargeType.red_back, ChargeType.red_get, ChargeType.transaction_reward
            , ChargeType.transfer_increase, ChargeType.return_gas, ChargeType.airdrop, ChargeType.gold_exchange
            , ChargeType.swap_reward,

            ChargeType.credit_in, ChargeType.user_credit_in)),
    // 支出
    pay(List.of(ChargeType.withdraw, ChargeType.purchase, ChargeType.transfer, ChargeType.repay
            , ChargeType.pledge, ChargeType.fund_purchase, ChargeType.agent_fund_redeem, ChargeType.agent_fund_interest
            , ChargeType.red_give, ChargeType.transfer_reduce, ChargeType.credit_out, ChargeType.user_credit_out));

    ChargeGroup(List<ChargeType> chargeTypes) {
        this.name = name();
        this.chargeTypes = chargeTypes;
    }

    public static ChargeGroup getInstance(ChargeType chargeType) {
        for (ChargeGroup chargeGroup : ChargeGroup.values()) {
            if (chargeGroup.chargeTypes.contains(chargeType)) {
                return chargeGroup;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    @Getter
    private final String name;

    @Getter
    private final List<ChargeType> chargeTypes;
}
