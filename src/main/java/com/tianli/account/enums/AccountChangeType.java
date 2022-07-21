package com.tianli.account.enums;

import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 余额类型
 */
@Getter
public enum AccountChangeType {
    // 理财
    financial("FI"),
    // 常规操作
    normal("NO"),
    // 借币
    borrow("BO");


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
