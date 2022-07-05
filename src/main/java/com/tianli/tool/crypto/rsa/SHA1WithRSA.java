package com.tianli.tool.crypto.rsa;

/**
 * Created by wangqiyun on 2018/1/16.
 */
public class SHA1WithRSA extends RSA {
    @Override
    public String getSign_algorithms() {
        return "SHA1WithRSA";
    }
}
