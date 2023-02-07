package com.tianli.charge.enums;

import com.tianli.account.enums.AccountChangeType;
import com.tianli.charge.vo.OrderStatusPullVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangqiyun
 * @since 2020/3/19 15:09
 */
public enum ChargeType {
    // recharge 充值 withdraw 提现 income 收益 purchase 申购 redeem 赎回 settle 结算 transfer 转存 borrow借币

    recharge("Deposition", "充值"),
    withdraw("Withdraw", "提币"),
    income("Earnings", "收益"),
    purchase("Subscription", "申购"),
    fund_purchase("Fund Subscription", "基金申购"),
    redeem("Redemption", "赎回"),
    fund_redeem("Fund Redemption", "基金赎回"),
    fund_interest("Fund Interest", "基金利息"),
    settle("Settlement", "结算"),
    transfer("transfer", "转存"),
    borrow("Borrow", "借币"),
    repay("Repay", "还币"),
    pledge("Collateral", "质押"),
    release("Release Pledge", "释放质押"),
    agent_fund_sale("Sales Revenue", "销售收入"),
    agent_fund_redeem("Redemption expense", "赎回支出"),
    agent_fund_interest("Interest payments", "利息支付"),
    red_give("Send Red Packet", "红包发送"),
    red_get("Red Packet Collection", "红包领取"),
    red_back("Red Packet Refund", "红包退款"),
    transaction_reward("Trading Bonus", "交易奖励", AccountChangeType.transaction_reward),
    transfer_increase("Transfer Increase", "划转增加",AccountChangeType.transfer_increase),
    transfer_reduce("Transfer Reduce", "划转减少",AccountChangeType.transfer_reduce),
    return_gas("Return Gas", "免Gas费",AccountChangeType.return_gas),
    airdrop("Airdrop", "空投",AccountChangeType.airdrop)
    // 增加类型需要在 ChargeRemarks 中增加对应的状态和文字，不然会报错
    // 增加类型需要在 ChargeGroup 中增加对应，不然会报错
    ;


    ChargeType(String nameEn, String nameZn) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
        this.accountChangeType = null;
    }

    ChargeType(String nameEn, String nameZn, AccountChangeType accountChangeType) {
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
