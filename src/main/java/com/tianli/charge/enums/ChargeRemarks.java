package com.tianli.charge.enums;


import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;


public enum ChargeRemarks {

    recharge_success("充值成功", "Successful Recharge", ChargeType.recharge, ChargeStatus.chain_success),
    redeem_process("赎回中", "In Redemption", ChargeType.redeem, ChargeStatus.chaining),
    redeem_success("赎回成功", "Successful Redemption", ChargeType.redeem, ChargeStatus.chain_success),
    redeem_fail("赎回失败", "Failed Redemption", ChargeType.redeem, ChargeStatus.chain_fail),
    fund_redeem_process("赎回中", "In Redemption", ChargeType.fund_redeem, ChargeStatus.chaining),
    fund_redeem_success("赎回成功", "Successful Redemption", ChargeType.fund_redeem, ChargeStatus.chain_success),
    fund_redeem_fail("赎回失败", "Failed Redemption", ChargeType.fund_redeem, ChargeStatus.chain_fail),
    fund_interest_success("发放成功", "Successful Release", ChargeType.fund_interest, ChargeStatus.chain_success),
    settle_success("结算成功", "Successful Settlement", ChargeType.settle, ChargeStatus.chain_success),
    red_back_success("退款成功", "Successful Refund", ChargeType.red_back, ChargeStatus.chain_success),
    red_get_success("领取成功", "Successful Received", ChargeType.red_get, ChargeStatus.chain_success),
    agent_fund_sale_success("销售收入", "Income Arrival", ChargeType.agent_fund_sale, ChargeStatus.chain_success),
    income_success("收益到账", "Earnings Arrival", ChargeType.income, ChargeStatus.chain_success),


    withdraw_chaining("提币中", "In Withdrawal", ChargeType.withdraw, ChargeStatus.chaining),
    withdraw_created("提币中", "In Withdrawal", ChargeType.withdraw, ChargeStatus.created),
    withdraw_success("提币成功", "Successful Withdrawal", ChargeType.withdraw, ChargeStatus.chain_success),
    withdraw_fail("提币失败", "Failed Withdrawal", ChargeType.withdraw, ChargeStatus.chain_fail),
    withdraw_review_fail("审核失败", "Failed Withdrawal", ChargeType.withdraw, ChargeStatus.review_fail),
    purchase_chaining("申购中", "In Subscription", ChargeType.purchase, ChargeStatus.chaining),
    purchase_created("申购中", "In Subscription", ChargeType.purchase, ChargeStatus.created),
    purchase_success("申购成功", "Successful Subscription", ChargeType.purchase, ChargeStatus.chain_success),
    purchase_fail("申购失败", "Failed Subscription", ChargeType.purchase, ChargeStatus.chain_fail),
    fund_purchase_chaining("申购中", "In Subscription", ChargeType.fund_purchase, ChargeStatus.chaining),
    fund_purchase_success("申购成功", "Successful Subscription", ChargeType.fund_purchase, ChargeStatus.chain_success),
    fund_purchase_fail("申购失败", "Failed Subscription", ChargeType.fund_purchase, ChargeStatus.chain_fail),
    fund_purchase_created("申购中", "Failed Subscription", ChargeType.fund_purchase, ChargeStatus.created),
    transfer_success("转存成功", "Successful Transfer", ChargeType.transfer, ChargeStatus.chain_success),
    transfer_fail("转存失败", "Failed Transfer ", ChargeType.transfer, ChargeStatus.chain_fail),
    red_give_chaining("发送中", "In Send", ChargeType.red_give, ChargeStatus.chaining),
    red_give_success("发送成功", "Successful Send", ChargeType.red_give, ChargeStatus.chain_success),
    red_give_fail("发送失败", "Failed Send ", ChargeType.red_give, ChargeStatus.chain_fail),
    agent_fund_redeem_success("支付成功", "Successful Payment", ChargeType.agent_fund_redeem, ChargeStatus.chain_success),
    agent_fund_redeem_fail("支付失败", "Failed Payment ", ChargeType.agent_fund_redeem, ChargeStatus.chain_fail),
    agent_fund_interest_success("支付成功", "Successful Payment", ChargeType.agent_fund_interest, ChargeStatus.chain_success),
    agent_fund_interest_fail("支付失败", "Failed Payment", ChargeType.agent_fund_interest, ChargeStatus.chain_fail),
    transfer_increase_success("划转增加", "Transfer Increase", ChargeType.transfer_increase, ChargeStatus.chain_success),
    transfer_reduce_success("划转减少", "Transfer Reduce", ChargeType.transfer_reduce, ChargeStatus.chain_success),
    return_gas_success("Gas费已到账", "Gas Fee Received", ChargeType.return_gas, ChargeStatus.chain_success),
    airdrop_success("空投到账", "Airdrop Received", ChargeType.airdrop, ChargeStatus.chain_success),
    gold_exchange_success("金币兑换到账", "Gold Exchange Received", ChargeType.gold_exchange, ChargeStatus.chain_success),
    points_sale_success("积分销售成功", "Points sale successful", ChargeType.points_sale, ChargeStatus.chain_success),
    points_withdrawal_success(" 积分提现成功", "Points withdrawal successful", ChargeType.points_withdrawal, ChargeStatus.chain_success),
    points_payment_success("积分购买成功", "Points payment successful", ChargeType.points_payment, ChargeStatus.chain_success),
    points_return_success("积分返还成功", "Points return successful", ChargeType.points_return, ChargeStatus.chain_success),
    swap_reward_success("奖励到账", "Reward return successful", ChargeType.swap_reward, ChargeStatus.chain_success),
    ;

    ChargeRemarks(String remarks, String remarksEn, ChargeType type, ChargeStatus status) {
        this.remarks = remarks;
        this.remarksEn = remarksEn;
        this.status = status;
        this.type = type;
    }

    @Getter
    private final String remarks;
    @Getter
    private final String remarksEn;
    private final ChargeType type;
    private final ChargeStatus status;

    public static ChargeRemarks getInstance(ChargeType type, ChargeStatus status) {
        ChargeRemarks[] values = ChargeRemarks.values();
        for (ChargeRemarks remarks : values) {
            if (remarks.status.equals(status) && remarks.type.equals(type)) {
                return remarks;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

}
