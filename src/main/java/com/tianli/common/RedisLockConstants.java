package com.tianli.common;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-22
 **/
public class RedisLockConstants {

    public static String PRODUCT_CLOSE_LOCK_PREFIX = "product:close:";

    public static String FINANCIAL_INCOME_TASK = "FinancialIncomeTask#currencyInterestStat:pageFlag:";

    public static String FINANCIAL_PRODUCT_BOARD_GET = "FinancialBoardProduct:getToday";

    public static String FINANCIAL_WALLET_BOARD_GET = "FinancialBoardWallet:getToday";

    public static String FUND_INCOME_TASK = "FundIncomeTask#incomeTasks:pageFlag:";

    public static String FUND_UPDATE_LOCK = "Fund:update:lock:";

    public static String FUND_CREATE_LOCK = "Fund:create:lock:";

    public static String FUND_REDEEM_LOCK = "Fund:redeem:lock:";

    public static String FUND_INCOME_LOCK = "Fund:income:lock:";

    public static String RECYCLE_LOCK = "recharge:recycle:";

    public static String PRODUCT_REDEEM = "product:redeem:";
    public static String PRODUCT_PURCHASE = "product:purchase:";
    public static String PRODUCT_WITHDRAW = "product:withdraw:";

    public static String RED_ENVELOPE_GIVE = "red:envelope:give:";
    public static String RED_ENVELOPE_EXPIRATION = "red:envelope:expiration:";


    public static final String ORDER_ADVANCE = "order:advance";

    public static final String ORDER_WITHDRAW = "order:withdraw";

    public static final String LOCK_REWARD = "lock:reward:";

    public static final String LOCK_BORROW = "lock:borrow:";

}
