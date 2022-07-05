package com.tianli.dividends.settlement.mapper;

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
public enum  LowSettlementChargeType {
    transfer_into("转入"),
    transfer_out("转出"),
    ;

    public String desc;

    LowSettlementChargeType(String desc){
        this.desc = desc;
    }
}
