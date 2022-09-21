package com.tianli.charge.enums;

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

    //充值
    recharge("Deposition", "充值"),
    //提现
    withdraw("Withdraw", "提币"),
    // 收益
    income("Earning", "收益"),
    // 申购
    purchase("Subscription", "申购"),
    // 基金申购
    fund_purchase("FundSubscription", "基金申购"),
    // 赎回
    redeem("Redemption", "赎回"),
    fund_redeem("FundRedemption", "基金赎回"),
    fund_interest("fundInterest", "基金利息"),
    // 结算
    settle("Settlement", "结算"),
    //转存
    transfer("transfer", "转存"),
    //借币
    borrow("Borrow", "借币"),
    //还币
    repay("Repay", "还币"),
    //借币冻结质押
    pledge("BorrowPledge", "质押"),
    //还币释放质押
    release("RepayPledge", "释放质押"),
    agent_fund_sale("AgentFundSale", "代理基金销售"),
    agent_fund_redeem("AgentFundRedemption", "代理基金赎回"),
    agent_fund_interest("agentFundInterest", "代理基金支付利息"),
    ;


    ChargeType(String nameEn, String nameZn) {
        this.nameZn = nameZn;
        this.nameEn = nameEn;
    }

    @Getter
    private final String nameZn;
    @Getter
    private final String nameEn;


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
