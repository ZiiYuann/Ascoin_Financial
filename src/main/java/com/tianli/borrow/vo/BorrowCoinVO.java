package com.tianli.borrow.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BorrowCoinVO {
    //存款市场总额
    private BigDecimal totalDepositAmount;
    //借款市场总额
    private BigDecimal totalBorrowAmount;
    //存款金额
    private BigDecimal depositAmount;
    //借款金额
    private BigDecimal borrowAmount;
    //质押金额
    private BigDecimal pledgeAmount;
    //质押金额
    private BigDecimal depositQuota;
    //已借额度比率
    @BigDecimalFormat("#.##%")
    private Double borrowRate;
    //订单
    private List<BorrowOrder> borrowOrders;
    @Data
    public static class BorrowOrder{
        //币别
        private String coin;
        //借出数量
        private BigDecimal borrowAmount;
        //质押数量
        private BigDecimal pledgeAmount;
        //利息
        private BigDecimal interest;
        //质押率
        @BigDecimalFormat("#.##%")
        private BigDecimal pledgeRate;

    }
}
