package com.tianli.currency.log;

import lombok.Getter;

@Getter
public enum CurrencyLogDes {
    充值,
    // normal 普通余额
    提现, 提现审核失败, 交易, 押注奖励, 平台返佣, 提现手续费, 利息, 抽水, 线下充值, 人工撤回, 线下提现,提现审核成功,现货交易,
    // deposit 押金余额
    平账, 平账归还, 缴纳, 撤回,
    // settlement 分红/结算余额
    结算, 结算归还, 转入, 转出,借款,还款,押注结算,
    // 划入, 划出
    划入, 划出, 买入, 赎回前扣除, 赎回, 收益,
    提现交易失败,
    //新币状态
    扣款,回款,奖励,
    ;
}
