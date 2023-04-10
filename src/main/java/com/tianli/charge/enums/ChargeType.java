package com.tianli.charge.enums;

import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.query.BalanceOperationChargeTypeQuery;
import com.tianli.charge.vo.OrderStatusPullVO;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangqiyun
 * @since 2020/3/19 15:09
 */
public enum ChargeType {
    // recharge 充值 withdraw 提现 income 收益 purchase 申购 redeem 赎回 settle 结算 transfer 转存 borrow借币

    recharge("Deposition", "充值成功"),
    withdraw("Withdraw", "提币"),
    income("Earnings", "收益"),
    purchase("Subscription", "申购"),
    fund_purchase("Fund Subscription", "基金申购"),
    redeem("Redemption", "赎回"),
    fund_redeem("Fund Redemption", "基金赎回"),
    fund_interest("Fund Interest", "基金利息"),
    settle("Settlement", "结算"),
    transfer("transfer", "转存"),
    agent_fund_sale("Sales Revenue", "销售收入"),
    agent_fund_redeem("Redemption expense", "赎回支出"),
    agent_fund_interest("Interest payments", "利息支付"),
    red_give("Send Red Packet", "红包发送"),
    red_get("Red Packet Collection", "红包领取"),
    red_back("Red Packet Refund", "红包退款"),
    transaction_reward("Trading Bonus", "交易奖励", AccountChangeType.transaction_reward),
    transfer_increase("Transfer Increase", "划转增加", AccountChangeType.transfer_increase),
    transfer_reduce("Transfer Reduce", "划转减少", AccountChangeType.transfer_reduce),
    return_gas("Return Gas", "免Gas费", AccountChangeType.return_gas),
    gold_exchange("Gold Exchange", "金币兑换", AccountChangeType.gold_exchange),
    // 商户 user_credit_in 加钱 user_credit_out 减钱
    user_credit_in("User credit in", "用户上分划入", AccountChangeType.user_credit_in),
    user_credit_out("User credit out", "用户下分划出", AccountChangeType.user_credit_out),
    // 用户 credit_out 减钱 credit_in 加钱
    credit_out("Credit out", "上分划出", AccountChangeType.credit_out),
    credit_in("Credit in", "下分划入", AccountChangeType.credit_in),
    airdrop("Airdrop", "空投", AccountChangeType.airdrop),
    swap_reward("Swap reward", "幸运闪兑奖励", AccountChangeType.swap_reward),

    // 借贷 borrow 借币 repay 还币 pledge 锁定质押物 release 释放质押物 forced_closeout 强制平仓 auto_re 自动补仓
    borrow("Borrow", "借币", AccountChangeType.borrow),
    repay("Repay", "还币", AccountChangeType.recharge),
    pledge("Collateral", "锁定质押物", AccountChangeType.borrow_pledge),
    release("Release Pledge", "释放质押物", AccountChangeType.release),
    forced_closeout("Forced Closeout", "强制平仓", AccountChangeType.forced_closeout),
    auto_re("Automatic replenishment", "自动补仓", AccountChangeType.auto_re),

    // 增加类型需要在 ChargeRemarks 中增加对应的状态和文字，不然会报错
    // 增加类型需要在 ChargeGroup 中增加对应，不然会报错


    // 流水特殊处理使用，涉及到特殊的使用需要在AccountDetailsNewQuery.chargeTypeQueries 做处理
    withdraw_success("Successful Withdrawal", "提币成功", AccountOperationType.reduce),
    withdraw_failed("Failed Withdrawal", "提币失败", AccountOperationType.unfreeze),
    withdraw_freeze("Freeze Withdrawal", "提币冻结", AccountOperationType.freeze),
    ;

    public ChargeType accountWrapper(AccountOperationType accountOperationType) {
        if (withdraw.equals(this)) {
            if (accountOperationType.equals(withdraw_success.getAccountOperationType())) {
                return withdraw_success;
            }
            if (accountOperationType.equals(withdraw_failed.getAccountOperationType())) {
                return withdraw_failed;
            }
            if (accountOperationType.equals(withdraw_freeze.getAccountOperationType())) {
                return withdraw_freeze;
            }
        }

        return this;
    }

    public static BalanceOperationChargeTypeQuery balanceOperationChargeTypeQuery(ChargeType chargeType) {
        if (withdraw_success.equals(chargeType) || withdraw_failed.equals(chargeType) || withdraw_freeze.equals(chargeType)) {
            return new BalanceOperationChargeTypeQuery(ChargeType.withdraw, chargeType.accountOperationType);
        }
        return null;
    }

    ChargeType(String nameEn, String nameZn, AccountOperationType accountOperationType) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = null;
        this.accountOperationType = accountOperationType;
    }

    ChargeType(String nameEn, String nameZn) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = null;
        this.accountOperationType = null;
    }

    ChargeType(String nameEn, String nameZn, AccountChangeType accountChangeType) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = accountChangeType;
        this.accountOperationType = null;
    }


    @Getter
    private final String nameZn;
    @Getter
    private final String nameEn;
    @Getter
    private final AccountChangeType accountChangeType;
    @Getter
    private final AccountOperationType accountOperationType;


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

    public static ChargeType getInstance(String type) {
        ChargeType[] values = ChargeType.values();
        for (ChargeType value : values) {
            if (type.equals(value.name())) {
                return value;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }
}
