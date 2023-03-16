package com.tianli.charge.enums;


import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;


public enum NewChargeRemarks {

    recharge_success("充值成功", "Successful Recharge", NewChargeType.recharge, NewChargeStatus.chain_success),
    redeem_process("赎回中", "In Redemption", NewChargeType.redeem, NewChargeStatus.chaining),
    redeem_success("赎回成功", "Successful Redemption", NewChargeType.redeem, NewChargeStatus.chain_success),
    redeem_fail("赎回失败", "Failed Redemption", NewChargeType.redeem, NewChargeStatus.chain_fail),
//    fund_redeem_process("赎回中", "In Redemption", NewChargeType.fund_redeem, NewChargeStatus.chaining),
//    fund_redeem_success("赎回成功", "Successful Redemption", NewChargeType.fund_redeem, NewChargeStatus.chain_success),
//    fund_redeem_fail("赎回失败", "Failed Redemption", NewChargeType.fund_redeem, NewChargeStatus.chain_fail),
    fund_interest_success("发放成功", "Successful Release", NewChargeType.fund_interest, NewChargeStatus.chain_success),
    settle_success("结算成功", "Successful Settlement", NewChargeType.settle, NewChargeStatus.chain_success),
    red_back_success("退款成功", "Successful Refund", NewChargeType.red_back, NewChargeStatus.chain_success),
    red_get_success("领取成功", "Successful Received", NewChargeType.red_get, NewChargeStatus.chain_success),
    agent_fund_sale_success("销售收入", "Income Arrival", NewChargeType.agent_fund_sale, NewChargeStatus.chain_success),
    income_success("收益到账", "Earnings Arrival", NewChargeType.income, NewChargeStatus.chain_success),


    withdraw_chaining("提币中", "In Withdrawal", NewChargeType.withdraw, NewChargeStatus.chaining),
    withdraw_created("提币中", "In Withdrawal", NewChargeType.withdraw, NewChargeStatus.created),

    withdraw_freeze("提币冻结", "In Withdrawal", NewChargeType.withdraw, NewChargeStatus.withdraw_freeze),

    withdraw_success("提币成功", "Successful Withdrawal", NewChargeType.withdraw, NewChargeStatus.withdraw_success),
    withdraw_fail("提币失败", "Failed Withdrawal", NewChargeType.withdraw, NewChargeStatus.withdraw_failed),
    withdraw_review_fail("审核失败", "Failed Withdrawal", NewChargeType.withdraw, NewChargeStatus.review_fail),
    purchase_chaining("申购中", "In Subscription", NewChargeType.purchase, NewChargeStatus.chaining),
    purchase_created("申购中", "In Subscription", NewChargeType.purchase, NewChargeStatus.created),
    purchase_success("申购成功", "Successful Subscription", NewChargeType.purchase, NewChargeStatus.chain_success),
    purchase_fail("申购失败", "Failed Subscription", NewChargeType.purchase, NewChargeStatus.chain_fail),
    fund_purchase_chaining("申购中", "In Subscription", NewChargeType.fund_purchase, NewChargeStatus.chaining),
    fund_purchase_success("申购成功", "Successful Subscription", NewChargeType.fund_purchase, NewChargeStatus.chain_success),
    fund_purchase_fail("申购失败", "Failed Subscription", NewChargeType.fund_purchase, NewChargeStatus.chain_fail),
    fund_purchase_created("申购中", "Failed Subscription", NewChargeType.fund_purchase, NewChargeStatus.created),
//    transfer_success("转存成功", "Successful Transfer", NewChargeType.transfer, NewChargeStatus.chain_success),
//    transfer_fail("转存失败", "Failed Transfer ", NewChargeType.transfer, NewChargeStatus.chain_fail),
    red_give_chaining("发送中", "In Send", NewChargeType.red_give, NewChargeStatus.chaining),
    red_give_success("发送成功", "Successful Send", NewChargeType.red_give, NewChargeStatus.chain_success),
    red_give_fail("发送失败", "Failed Send ", NewChargeType.red_give, NewChargeStatus.chain_fail),
    agent_fund_redeem_success("支付成功", "Successful Payment", NewChargeType.agent_fund_redeem, NewChargeStatus.chain_success),
    agent_fund_redeem_fail("支付失败", "Failed Payment ", NewChargeType.agent_fund_redeem, NewChargeStatus.chain_fail),
    agent_fund_interest_success("支付成功", "Successful Payment", NewChargeType.agent_fund_interest, NewChargeStatus.chain_success),
    agent_fund_interest_fail("支付失败", "Failed Payment", NewChargeType.agent_fund_interest, NewChargeStatus.chain_fail),
    transfer_increase_success("划转增加", "Transfer Increase", NewChargeType.transfer_increase, NewChargeStatus.chain_success),
    transfer_reduce_success("划转减少", "Transfer Reduce", NewChargeType.transfer_reduce, NewChargeStatus.chain_success),
    return_gas_success("Gas费已到账", "Gas Fee Received", NewChargeType.return_gas, NewChargeStatus.chain_success),
    airdrop_success("空投到账", "Airdrop Received", NewChargeType.airdrop, NewChargeStatus.chain_success),
    gold_exchange_success("金币兑换到账", "Gold Exchange Received", NewChargeType.gold_exchange, NewChargeStatus.chain_success),
    user_credit_in_success("用户上分划入成功", "User credit in successful", NewChargeType.user_credit_in, NewChargeStatus.chain_success),
    user_credit_out_success(" 用户下分划出成功", "User credit out successful", NewChargeType.user_credit_out, NewChargeStatus.chain_success),
    credit_out_success("上分划出成功", "Credit out successful", NewChargeType.credit_out, NewChargeStatus.chain_success),
    credit_in_success("下分划入成功", "Credit in successful", NewChargeType.credit_in, NewChargeStatus.chain_success),
    swap_reward_success("奖励到账", "Reward return successful", NewChargeType.swap_reward, NewChargeStatus.chain_success),
    borrow_success("借币到账", "Debit Arrival", NewChargeType.borrow, NewChargeStatus.chain_success),
    repay_success("还币成功", "Repay success", NewChargeType.repay, NewChargeStatus.chain_success),
    pledge_success("锁定质押物", "Locked collaterals", NewChargeType.pledge, NewChargeStatus.chain_success),
    release_success("释放质押物", "Released collaterals", NewChargeType.release, NewChargeStatus.chain_success),
//    auto_re_success("补仓成功", "Successful position filling", NewChargeType.auto_re, NewChargeStatus.chain_success),
//    forced_closeout_success("平仓完成", "Closeout completed", NewChargeType.forced_closeout, NewChargeStatus.chain_success),

    ;

    NewChargeRemarks(String remarks, String remarksEn, NewChargeType type, NewChargeStatus status) {
        this.remarks = remarks;
        this.remarksEn = remarksEn;
        this.status = status;
        this.type = type;
    }

    @Getter
    private final String remarks;
    @Getter
    private final String remarksEn;
    private final NewChargeType type;
    private final NewChargeStatus status;

    public static NewChargeRemarks getInstance(NewChargeType type, NewChargeStatus status) {
        NewChargeRemarks[] values = NewChargeRemarks.values();
        for (NewChargeRemarks remarks : values) {
            if (remarks.status.name().equals(status.name()) && remarks.type.name().equals(type.name())) {
                return remarks;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

}
