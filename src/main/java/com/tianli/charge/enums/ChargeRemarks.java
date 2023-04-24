package com.tianli.charge.enums;


import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;


public enum ChargeRemarks {

    RECHARGE_SUCCESS("充值成功", "Successful Recharge", ChargeType.recharge, ChargeStatus.chain_success),
    REDEEM_PROCESS("赎回中", "In Redemption", ChargeType.redeem, ChargeStatus.chaining),
    REDEEM_SUCCESS("赎回成功", "Successful Redemption", ChargeType.redeem, ChargeStatus.chain_success),
    REDEEM_FAIL("赎回失败", "Failed Redemption", ChargeType.redeem, ChargeStatus.chain_fail),
    FUND_REDEEM_PROCESS("赎回中", "In Redemption", ChargeType.fund_redeem, ChargeStatus.chaining),
    FUND_REDEEM_SUCCESS("赎回成功", "Successful Redemption", ChargeType.fund_redeem, ChargeStatus.chain_success),
    FUND_REDEEM_FAIL("赎回失败", "Failed Redemption", ChargeType.fund_redeem, ChargeStatus.chain_fail),
    FUND_INTEREST_SUCCESS("发放成功", "Successful Release", ChargeType.fund_interest, ChargeStatus.chain_success),
    SETTLE_SUCCESS("结算成功", "Successful Settlement", ChargeType.settle, ChargeStatus.chain_success),
    RED_BACK_SUCCESS("退款成功", "Successful Refund", ChargeType.red_back, ChargeStatus.chain_success),
    RED_GET_SUCCESS("领取成功", "Successful Received", ChargeType.red_get, ChargeStatus.chain_success),
    AGENT_FUND_SALE_SUCCESS("销售收入", "Income Arrival", ChargeType.agent_fund_sale, ChargeStatus.chain_success),
    INCOME_SUCCESS("收益到账", "Earnings Arrival", ChargeType.income, ChargeStatus.chain_success),


    WITHDRAW_CHAINING("提币中", "In Withdrawal", ChargeType.withdraw, ChargeStatus.chaining),
    WITHDRAW_CREATED("提币中", "In Withdrawal", ChargeType.withdraw, ChargeStatus.created),
    WITHDRAW_SUCCESS("提币成功", "Successful Withdrawal", ChargeType.withdraw, ChargeStatus.chain_success),
    WITHDRAW_FAIL("提币失败", "Failed Withdrawal", ChargeType.withdraw, ChargeStatus.chain_fail),
    WITHDRAW_REVIEW_FAIL("审核失败", "Failed Withdrawal", ChargeType.withdraw, ChargeStatus.review_fail),
    PURCHASE_CHAINING("申购中", "In Subscription", ChargeType.purchase, ChargeStatus.chaining),
    PURCHASE_CREATED("申购中", "In Subscription", ChargeType.purchase, ChargeStatus.created),
    PURCHASE_SUCCESS("申购成功", "Successful Subscription", ChargeType.purchase, ChargeStatus.chain_success),
    PURCHASE_FAIL("申购失败", "Failed Subscription", ChargeType.purchase, ChargeStatus.chain_fail),
    FUND_PURCHASE_CHAINING("申购中", "In Subscription", ChargeType.fund_purchase, ChargeStatus.chaining),
    FUND_PURCHASE_SUCCESS("申购成功", "Successful Subscription", ChargeType.fund_purchase, ChargeStatus.chain_success),
    FUND_PURCHASE_FAIL("申购失败", "Failed Subscription", ChargeType.fund_purchase, ChargeStatus.chain_fail),
    FUND_PURCHASE_CREATED("申购中", "Failed Subscription", ChargeType.fund_purchase, ChargeStatus.created),
    TRANSFER_SUCCESS("转存成功", "Successful Transfer", ChargeType.transfer, ChargeStatus.chain_success),
    TRANSFER_FAIL("转存失败", "Failed Transfer ", ChargeType.transfer, ChargeStatus.chain_fail),
    RED_GIVE_CHAINING("发送中", "In Send", ChargeType.red_give, ChargeStatus.chaining),
    RED_GIVE_SUCCESS("发送成功", "Successful Send", ChargeType.red_give, ChargeStatus.chain_success),
    RED_GIVE_FAIL("发送失败", "Failed Send ", ChargeType.red_give, ChargeStatus.chain_fail),
    AGENT_FUND_REDEEM_SUCCESS("支付成功", "Successful Payment", ChargeType.agent_fund_redeem, ChargeStatus.chain_success),
    AGENT_FUND_REDEEM_FAIL("支付失败", "Failed Payment ", ChargeType.agent_fund_redeem, ChargeStatus.chain_fail),
    AGENT_FUND_INTEREST_SUCCESS("支付成功", "Successful Payment", ChargeType.agent_fund_interest, ChargeStatus.chain_success),
    AGENT_FUND_INTEREST_FAIL("支付失败", "Failed Payment", ChargeType.agent_fund_interest, ChargeStatus.chain_fail),
    TRANSFER_INCREASE_SUCCESS("划转增加", "Transfer Increase", ChargeType.transfer_increase, ChargeStatus.chain_success),
    TRANSFER_REDUCE_SUCCESS("划转减少", "Transfer Reduce", ChargeType.transfer_reduce, ChargeStatus.chain_success),
    RETURN_GAS_SUCCESS("Gas费已到账", "Gas Fee Received", ChargeType.return_gas, ChargeStatus.chain_success),
    AIRDROP_SUCCESS("空投到账", "Airdrop Received", ChargeType.airdrop, ChargeStatus.chain_success),
    GOLD_EXCHANGE_SUCCESS("金币兑换到账", "Gold Exchange Received", ChargeType.gold_exchange, ChargeStatus.chain_success),
    USER_CREDIT_IN_SUCCESS("用户上分划入成功", "User credit in successful", ChargeType.user_credit_in, ChargeStatus.chain_success),
    USER_CREDIT_OUT_SUCCESS(" 用户下分划出成功", "User credit out successful", ChargeType.user_credit_out, ChargeStatus.chain_success),
    CREDIT_OUT_SUCCESS("上分划出成功", "Credit out successful", ChargeType.credit_out, ChargeStatus.chain_success),
    CREDIT_IN_SUCCESS("下分划入成功", "Credit in successful", ChargeType.credit_in, ChargeStatus.chain_success),
    SWAP_REWARD_SUCCESS("奖励到账", "Reward return successful", ChargeType.swap_reward, ChargeStatus.chain_success),
    BORROW_SUCCESS("借币到账", "Debit Arrival", ChargeType.borrow, ChargeStatus.chain_success),
    REPAY_SUCCESS("还币成功", "Repay success", ChargeType.repay, ChargeStatus.chain_success),
    PLEDGE_SUCCESS("锁定质押物", "Locked collaterals", ChargeType.pledge, ChargeStatus.chain_success),
    RELEASE_SUCCESS("释放质押物", "Released collaterals", ChargeType.release, ChargeStatus.chain_success),
    AUTO_RE_SUCCESS("补仓成功", "Successful position filling", ChargeType.auto_re, ChargeStatus.chain_success),
    assure_withdraw_success("提币成功", "Successful Withdrawal", ChargeType.assure_withdraw, ChargeStatus.chain_success),
    assure_recharge_success("充值成功", "Successful Recharge", ChargeType.assure_recharge, ChargeStatus.chain_success),

    c2c_transfer_in("c2c转入", "C2C Transfer In", ChargeType.c2c_transfer_in, ChargeStatus.chain_success),

    c2c_transfer_out("c2c转出", "C2C Transfer Out", ChargeType.c2c_transfer_out, ChargeStatus.chain_success),

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
