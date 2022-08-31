package com.tianli.fund.contant;

public interface FundTransactionStatus {

    Integer success = 1;

    Integer processing = 2;

    Integer wait_audit = 3;

    Integer fail = 4;

}
