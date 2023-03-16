package com.tianli.charge.enums;

import com.tianli.account.enums.AccountChangeType;
import com.tianli.charge.vo.OrderStatusPullVO;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangkang
 * @since 2022/3/14 15:09
 */
public enum NewChargeType {
    // recharge 充值 withdraw 提现 income 收益 purchase 申购 redeem 赎回 settle 结算 transfer 转存 borrow借币

    recharge("Deposition", "充值成功"),
    withdraw("Withdraw", "提币"),
    income("Earnings", "收益"),
    purchase("Subscription", "理财申购"),
    fund_purchase("Fund Subscription", "基金申购"),
    redeem("Redemption", "赎回"),
    fund_interest("Fund Interest", "基金利息"),
    settle("Settlement", "结算"),
    agent_fund_sale("Sales Revenue", "用户申购"),
    agent_fund_redeem("Redemption expense", "用户赎回"),
    agent_fund_interest("Interest payments", "利息支付"),
    red_give("Send Red Packet", "红包发送"),
    red_get("Red Packet Collection", "红包领取"),
    red_back("Red Packet Refund", "红包退款"),
    transaction_reward("Trading Bonus", "交易奖励", AccountChangeType.transaction_reward),
    transfer_increase("Transfer Increase", "划转",AccountChangeType.transfer_increase),
    transfer_reduce("Transfer Reduce", "划转",AccountChangeType.transfer_reduce),
    return_gas("Return Gas", "免Gas费",AccountChangeType.return_gas),
    gold_exchange("Gold Exchange", "金币兑换",AccountChangeType.gold_exchange),
    // 商户 user_credit_in 加钱 user_credit_out 减钱
    user_credit_in("User credit in","用户上分划入",AccountChangeType.user_credit_in),
    user_credit_out("User credit out","用户下分划出",AccountChangeType.user_credit_out),
    // 用户 credit_out 减钱 credit_in 加钱
    credit_out("Credit out","上分划出",AccountChangeType.credit_out),
    credit_in("Credit in","下分划入",AccountChangeType.credit_in),
    airdrop("Airdrop", "空投",AccountChangeType.airdrop),
    swap_reward("Swap reward", "闪兑交易奖励",AccountChangeType.swap_reward),

    // 借贷 borrow 借币 repay 还币 pledge 锁定质押物 release 释放质押物 forced_closeout 强制平仓 auto_re 自动补仓
    borrow("Borrow", "借币",AccountChangeType.borrow),
    repay("Repay", "还币",AccountChangeType.recharge),
    pledge("Collateral", "质押",AccountChangeType.borrow_pledge),
    release("Release Pledge", "质押解冻",AccountChangeType.release);


    NewChargeType(String nameEn, String nameZn) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = null;
    }

    NewChargeType(String nameEn, String nameZn, AccountChangeType accountChangeType) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = accountChangeType;
    }


    @Getter
    private final String nameZn;
    @Getter
    private final String nameEn;
    @Getter
    private final AccountChangeType accountChangeType;


    public static List<OrderStatusPullVO> orderStatusPull(NewChargeType chargeType) {
        List<OrderStatusPullVO> result = new ArrayList<>();
        // todo 国际化
        switch (chargeType) {
            case purchase:
            case redeem:
//            case transfer:
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

    public static NewChargeType getInstance(ChargeType type) {
        NewChargeType[] values = NewChargeType.values();
        for (NewChargeType newChargeType : values) {
            if (type.name().equals(newChargeType.name()) ) {
                return newChargeType;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }
}
