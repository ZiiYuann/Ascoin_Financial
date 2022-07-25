package com.tianli.common;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
public class IdGenerator {

    public static String financialIncomeAccrueId(){
        return "FIA" + CommonFunction.generalId();
    }

    public static String financialIncomeDailyId(){
        return "FID" + CommonFunction.generalId();
    }


}
