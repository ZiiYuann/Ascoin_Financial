package com.tianli.charge.enums;


public enum ChargeRemarks {

    recharge_success("充值成功", "Successful Recharge", ChargeType.purchase,ChargeStatus.chain_success),
    redeem_process("赎回中", "In Redemption", ChargeType.redeem,ChargeStatus.chaining),
    redeem_success("赎回成功", "Successful Redemption", ChargeType.redeem,ChargeStatus.chain_success),
    redeem_fail("赎回失败", "Failed Redemption", ChargeType.redeem,ChargeStatus.chain_fail),
    fund_redeem_process("赎回中", "In Redemption", ChargeType.fund_redeem,ChargeStatus.chaining),
    fund_redeem_success("赎回成功", "Successful Redemption", ChargeType.fund_redeem,ChargeStatus.chain_success),
    fund_redeem_fail("赎回失败", "Failed Redemption", ChargeType.fund_redeem,ChargeStatus.chain_fail),
    fund_interest_success("发放成功", "Successful Release", ChargeType.fund_interest,ChargeStatus.chain_success),
    settle_success("结算成功", "Successful Settlement", ChargeType.settle,ChargeStatus.chain_success),
    red_back_success("退款成功", "Successful Refund", ChargeType.red_back,ChargeStatus.chain_success),
    red_get_success("领取成功", "Successful Received", ChargeType.red_get,ChargeStatus.chain_success),
    agent_fund_sale_success("销售收入", "Income Arrival", ChargeType.agent_fund_sale,ChargeStatus.chain_success),
    tr_success("已发放{1}笔", "{1} Receiving Record", ChargeType.transaction_reward,ChargeStatus.chain_success),


    withdraw_chaining("提币中", "In Withdrawal", ChargeType.withdraw,ChargeStatus.chaining),
    withdraw_success("提币成功", "Successful Withdrawal", ChargeType.withdraw,ChargeStatus.chain_success),


    ;

    ChargeRemarks(String remarks, String remarksEn, ChargeType type, ChargeStatus status) {
        this.remarks = remarks;
        this.remarksEn = remarksEn;
        this.status = status;
        this.type = type;
    }

    private final String remarks;
    private final String remarksEn;
    private final ChargeType type;
    private final ChargeStatus status;

}
