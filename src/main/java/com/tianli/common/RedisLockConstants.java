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

    public static String BORROW_INCOME_TASK = "BorrowInterestTask#interestTasks:pageFlag:";

    public static String BORROW_INCOME_TASK_LOCK = "BorrowInterestTask#interestTasks:lock:";

    public static String BORROW_ORDER_CHANGE_LOCK = "BorrowOrder#change:lock:";

    public static String BORROW_ORDER_CREATE_LOCK = "BorrowOrder#create:lock:";
}
