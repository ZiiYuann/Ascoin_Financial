package com.tianli.account.enums;

import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 余额类型
 */
@Getter
public enum AccountChangeType {
    //理财
    purchase("PU"),
    //赎回
    redeem("RE"),
    //提现
    withdraw("WD"),
    //常规操作
    normal("NO"),
    //充值
    recharge("RE"),
    //收益
    income("IN"),
    //结算
    settle("SE"),
    //借币质押
    borrow_pledge("BF"),
    //借币
    borrow("BO"),
    //还币释放冻结
    release("RE"),
    //还币
    repay("RP")
    ;


    AccountChangeType(String prefix){
        this.prefix = prefix;
    }
    private final String prefix;

    public static AccountChangeType getInstanceBySn(String sn){
        if (StringUtils.isBlank(sn)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        for (AccountChangeType accountChangeType : AccountChangeType.values()) {
            if(sn.startsWith(accountChangeType.prefix)){
                return accountChangeType;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }
}
