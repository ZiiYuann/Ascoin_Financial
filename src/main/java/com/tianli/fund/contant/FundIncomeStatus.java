package com.tianli.fund.contant;

public interface FundIncomeStatus {

    //已计算
    Integer calculated = 1;

    //待审核
    Integer wait_audit = 2;

    //已发放
    Integer audit_success = 3;

    //审核失败
    Integer audit_failure = 4;

}
