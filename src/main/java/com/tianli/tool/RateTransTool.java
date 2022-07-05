package com.tianli.tool;

import com.tianli.exception.ErrorCodeEnum;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author chensong
 * @date 2020-12-29 10:01
 * @since 1.0.0
 */
public class RateTransTool {
    private RateTransTool(){
        ErrorCodeEnum.throwException("该对象不可被创建");
    }
    public static String multi(String rate){
        if(StringUtils.isBlank(rate)){
            return "0";
        }
        BigDecimal bigDecimal = new BigDecimal(rate);
        return bigDecimal.multiply(new BigDecimal("100")).toString();
    }

    public static String div(String rate){
        if(StringUtils.isBlank(rate)){
            return "0";
        }
        BigDecimal bigDecimal = new BigDecimal(rate);
        return bigDecimal.divide(new BigDecimal("100")).toString();
    }
}
