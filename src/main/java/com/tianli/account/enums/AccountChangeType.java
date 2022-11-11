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
    advance_purchase("APU"),
    //基金理财
    fund_purchase("FP"),
    //赎回
    redeem("RE"),
    //基金赎回
    fund_redeem("FE"),
    //基金利息
    fund_interest("FI"),
    //提现
    withdraw("WD"),
    //常规操作
    normal("NO"),
    //充值
    recharge("RE"),
    transfer("TR"),
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
    repay("RP"),
    //基金销售
    agent_fund_sale("AS"),
    //基金赎回
    agent_fund_redeem("AR"),
    //基金支付利息
    agent_fund_interest("AI"),
    red_give("RGI"),
    red_get("RGE"),
    red_back("RBA"),
    transaction_reward("TR"),;
    ;


    AccountChangeType(String prefix) {
        this.prefix = prefix;
    }

    private final String prefix;

    public static AccountChangeType getInstanceBySn(String sn) {
        if (StringUtils.isBlank(sn)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        for (AccountChangeType accountChangeType : AccountChangeType.values()) {
            if (sn.startsWith(accountChangeType.prefix)) {
                return accountChangeType;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }
}
