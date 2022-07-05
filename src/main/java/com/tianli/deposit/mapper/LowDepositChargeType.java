package com.tianli.deposit.mapper;

import lombok.Getter;

/**
 * <p>
 * 低级代理商保证金
 * </p>
 *
 * @author hd
 * @since 2020-12-11
 */
@Getter
public enum LowDepositChargeType {
    recharge("缴纳"),
    withdraw("撤回"),
    ;

    public String desc;

    LowDepositChargeType(String desc){
        this.desc = desc;
    }
}
