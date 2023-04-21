package com.tianli.charge.enums;

import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.query.BalanceOperationChargeTypeQuery;
import com.tianli.charge.vo.OrderStatusPullVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangqiyun
 * @since 2020/3/19 15:09
 */
@Getter
public enum ChargeType {
    // recharge 充值 withdraw 提现 income 收益 purchase 申购 redeem 赎回 settle 结算 transfer 转存 borrow借币

    recharge("Deposition", "充值成功", AccountChangeType.recharge,ChargeTypeGroupEnum.RECHARGE),
    withdraw("Withdraw", "提币", AccountChangeType.withdraw,ChargeTypeGroupEnum.WITHDRAW),
    income("Earnings", "理财收益", AccountChangeType.income,ChargeTypeGroupEnum.IN),
    purchase("User Subscription", "用户申购", AccountChangeType.purchase,ChargeTypeGroupEnum.OUT),
    fund_purchase("Fund Subscription", "基金申购", AccountChangeType.fund_purchase,ChargeTypeGroupEnum.OUT),
    redeem("Redemption", "赎回", AccountChangeType.redeem,ChargeTypeGroupEnum.IN),
    fund_redeem("Fund Redemption", "基金赎回", AccountChangeType.fund_redeem,ChargeTypeGroupEnum.IN),
    fund_interest("Fund Interest", "基金利息", AccountChangeType.fund_interest,ChargeTypeGroupEnum.IN),
    settle("Settlement", "结算本金", AccountChangeType.settle,ChargeTypeGroupEnum.IN),
    transfer("transfer", "转存", AccountChangeType.transfer,ChargeTypeGroupEnum.OUT),
    agent_fund_sale("Sales Revenue", "销售收入", AccountChangeType.agent_fund_sale,ChargeTypeGroupEnum.IN),
    agent_fund_redeem("Redemption expense", "赎回支出", AccountChangeType.agent_fund_redeem,ChargeTypeGroupEnum.OUT),
    agent_fund_interest("Interest payments", "利息支付", AccountChangeType.agent_fund_interest,ChargeTypeGroupEnum.OUT),
    red_give("Send Red Packet", "红包发送", AccountChangeType.red_give,ChargeTypeGroupEnum.OUT),
    red_get("Red Packet Collection", "红包领取", AccountChangeType.red_get,ChargeTypeGroupEnum.IN),
    red_back("Red Packet Refund", "红包退款", AccountChangeType.red_back,ChargeTypeGroupEnum.IN),
    transaction_reward("Trading Bonus", "交易奖励", AccountChangeType.transaction_reward,ChargeTypeGroupEnum.IN),
    transfer_increase("Transfer", "划转", AccountChangeType.transfer_increase,ChargeTypeGroupEnum.IN),
    transfer_reduce("Transfer", "划转", AccountChangeType.transfer_reduce,ChargeTypeGroupEnum.OUT),
    return_gas("Return Gas", "免Gas费", AccountChangeType.return_gas,ChargeTypeGroupEnum.IN),
    gold_exchange("Gold Exchange", "金币兑换", AccountChangeType.gold_exchange,ChargeTypeGroupEnum.IN),
    // 商户 user_credit_in 加钱 user_credit_out 减钱
    user_credit_in("User credit in", "用户上分划入", AccountChangeType.user_credit_in,ChargeTypeGroupEnum.IN),
    user_credit_out("User credit out", "用户下分划出", AccountChangeType.user_credit_out,ChargeTypeGroupEnum.OUT),
    // 用户 credit_out 减钱 credit_in 加钱
    credit_out("Credit out", "上分划出", AccountChangeType.credit_out,ChargeTypeGroupEnum.OUT),
    credit_in("Credit in", "下分划入", AccountChangeType.credit_in,ChargeTypeGroupEnum.IN),
    airdrop("Airdrop", "空投", AccountChangeType.airdrop,ChargeTypeGroupEnum.IN),
    swap_reward("Swap reward", "幸运闪兑奖励", AccountChangeType.swap_reward,ChargeTypeGroupEnum.IN),

    // 借贷 borrow 借币 repay 还币 pledge 锁定质押物 release 释放质押物 forced_closeout 强制平仓 auto_re 自动补仓
    borrow("Borrow", "借币", AccountChangeType.borrow,ChargeTypeGroupEnum.IN),
    repay("Repay", "还币", AccountChangeType.recharge,ChargeTypeGroupEnum.OUT),
    pledge("Collateral", "锁定质押物", AccountChangeType.borrow_pledge,ChargeTypeGroupEnum.OUT),
    release("Release Pledge", "释放质押物", AccountChangeType.release,ChargeTypeGroupEnum.IN),
    forced_closeout("Forced Closeout", "强制平仓", AccountChangeType.forced_closeout,ChargeTypeGroupEnum.IN),
    auto_re("Automatic replenishment", "自动补仓", AccountChangeType.auto_re,ChargeTypeGroupEnum.OUT),

    c2c_transfer_in("C2C Transfer In","c2c转入",AccountChangeType.c2c_transfer_in,ChargeTypeGroupEnum.IN),

    c2c_transfer_out("C2C Transfer Out","c2c转出",AccountChangeType.c2c_transfer_out,ChargeTypeGroupEnum.OUT),

    // 增加类型需要在 ChargeRemarks 中增加对应的状态和文字，不然会报错
    // 增加类型需要在 ChargeGroup 中增加对应，不然会报错


    // 流水特殊处理使用，涉及到特殊的使用需要在AccountDetailsNewQuery.chargeTypeQueries 做处理
    withdraw_success("Successful Withdrawal", "提币成功",null,null),
    withdraw_failed("Failed Withdrawal", "提币失败",null,null),
    withdraw_freeze("Freeze Withdrawal", "提币冻结",null,null),
    ;

    private final String nameZn;
    private final String nameEn;
    private final AccountChangeType accountChangeType;
    private final ChargeTypeGroupEnum level3Group;

    ChargeType(String nameEn, String nameZn, AccountChangeType accountChangeType,ChargeTypeGroupEnum level3Group) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = accountChangeType;
        this.level3Group = level3Group;
    }

    public ChargeType accountWrapper(AccountOperationType accountOperationType) {
        if (withdraw.equals(this)) {
            if (accountOperationType.equals(AccountOperationType.reduce)) {
                return withdraw_success;
            }
            if (accountOperationType.equals(AccountOperationType.unfreeze)) {
                return withdraw_failed;
            }
            if (accountOperationType.equals(AccountOperationType.freeze)) {
                return withdraw_freeze;
            }
        }

        return this;
    }

    public static BalanceOperationChargeTypeQuery balanceOperationChargeTypeQuery(ChargeType chargeType) {
        if (withdraw_success.equals(chargeType)) {
            return new BalanceOperationChargeTypeQuery(ChargeType.withdraw, AccountOperationType.reduce);
        }
        if (withdraw_failed.equals(chargeType)) {
            return new BalanceOperationChargeTypeQuery(ChargeType.withdraw, AccountOperationType.unfreeze);
        }
        if (withdraw_freeze.equals(chargeType)) {
            return new BalanceOperationChargeTypeQuery(ChargeType.withdraw, AccountOperationType.freeze);
        }
        return null;
    }

    public static List<OrderStatusPullVO> orderStatusPull(ChargeType chargeType) {
        List<OrderStatusPullVO> result = new ArrayList<>();
        // todo 国际化
        switch (chargeType) {
            case purchase:
            case redeem:
            case transfer:
            case settle:
                result.add(new OrderStatusPullVO(ChargeStatus.chain_success, "成功"));
                result.add(new OrderStatusPullVO(ChargeStatus.chain_fail, "失败"));
                result.add(new OrderStatusPullVO(ChargeStatus.chaining, "进行中"));
                break;
            case withdraw:
                result.add(new OrderStatusPullVO(ChargeStatus.chain_success, "提币成功"));
                result.add(new OrderStatusPullVO(ChargeStatus.chain_fail, "提币失败"));
                result.add(new OrderStatusPullVO(ChargeStatus.chaining, "提币中"));
                result.add(new OrderStatusPullVO(ChargeStatus.created, "待审核"));
                break;
            case recharge:
                result.add(new OrderStatusPullVO(ChargeStatus.chain_success, "充值成功"));
                result.add(new OrderStatusPullVO(ChargeStatus.chain_fail, "充值失败"));
                break;
            default:
                break;
        }
        return result;
    }

}
