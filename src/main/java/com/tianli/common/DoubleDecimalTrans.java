package com.tianli.common;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 *
 * </P>
 *
 * @author linyifan
 * @since 5/10/21 6:15 PM
 */

@Service
public class DoubleDecimalTrans {

    /**
     * 比例除以100
     */
    public static Double double_divide_hundred(Double amount){
        BigDecimal decimal_amount = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        return Double.valueOf(String.valueOf(decimal_amount));
    }

    /**
     * 比例乘以100
     */
    public static Double double_multiply_hundred(Double amount){
        BigDecimal decimal_amount = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal(100));
        return Double.valueOf(String.valueOf(decimal_amount));
    }

}
