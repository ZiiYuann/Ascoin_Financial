package com.tianli.charge.enums;

import lombok.Getter;

/**
 * @author  wangqiyun
 * @since  2020/3/19 15:09
 */
public enum ChargeType {
    // recharge 充值 withdraw 提现 income 收益 purchase 申购 redeem 赎回 settle 结算 transfer 转存 borrow借币

    //充值
    recharge("Deposition","充值"),
    //提现
    withdraw("Withdraw","提币"),
    // 收益
    income   ("Earning","收益"),
    // 申购
    purchase ("Subscription","申购"),
    // 赎回
    redeem ("Redemption","赎回"),
    // 结算
    settle("Settlement","结算"),
    //转存
    transfer ("transfer","转存"),
    //借币
    borrow("Borrow","借币"),
    //还币
    repay("Repay","还币"),;


    ChargeType(String nameEn,String nameZn){
        this.nameZn = nameZn;
        this.nameEn = nameEn;
    }
    @Getter
    private final String nameZn;
    @Getter
    private final String nameEn;
}
