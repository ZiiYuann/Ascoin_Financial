package com.tianli.tool;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigIntegerTool {

    public static BigInteger multiDouble(BigInteger source, double dou) {
        if(source == null){
            throw new NullPointerException();
        }
        return new BigDecimal(source).multiply(new BigDecimal(String.valueOf(dou))).toBigInteger();
    }
}
