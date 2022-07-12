package com.tianli.financial.enums;

import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

@Getter
public enum FinancialLogStatus {
    PURCHASE_PROCESSING((byte) 0,"申购中"),
    PURCHASE_SUCCESS((byte) 1,"申购成功"),
    PURCHASE_FAIL((byte) 2,"申购失败"),
    INTEREST_PROCESSING((byte)3,"记息中"),
    SETTLE_SUCCESS((byte)4 ,"结算成功"),
    REDEEM_PROCESSING((byte) 5,"赎回中"),
    REDEEM_SUCCESS((byte) 6,"赎回成功"),
    TRANSFER_SUCCESS((byte) 7,"转存成功");

    FinancialLogStatus(byte type,String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static FinancialLogStatus getByType(byte type){
        for (FinancialLogStatus f :
                FinancialLogStatus.values()) {
            if(f.type == type){
                return f;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    private final byte type;
    private final String desc;
}
