package com.tianli.tool.crypto.rsa;

/**
 * @Author wangqiyun
 * @Date 2019/1/4 2:03 PM
 */
public class SHA512WithRSA extends RSA {
    @Override
    public String getSign_algorithms() {
        return "SHA512WithRSA";
    }
}
