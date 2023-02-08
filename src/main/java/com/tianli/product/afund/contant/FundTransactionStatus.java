package com.tianli.product.afund.contant;

public interface FundTransactionStatus {
    //成功
    Integer success = 1;
    //进行中
    Integer processing = 2;
    //待审核
    Integer wait_audit = 3;
    //失败
    Integer fail = 4;

}
