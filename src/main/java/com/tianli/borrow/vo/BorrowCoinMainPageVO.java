package com.tianli.borrow.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BorrowCoinMainPageVO {
    //存款市场总额
    @BigDecimalFormat
    private BigDecimal totalDepositAmount;
    //借款市场总额
    @BigDecimalFormat
    private BigDecimal totalBorrowAmount;
    //存款金额
    @BigDecimalFormat
    private BigDecimal depositAmount;
    //借款金额
    @BigDecimalFormat
    private BigDecimal borrowAmount;
    //质押金额
    @BigDecimalFormat
    private BigDecimal pledgeAmount;
    //可借额度
    @BigDecimalFormat
    private BigDecimal borrowQuota;
    //已借额度比率
    @BigDecimalFormat("#.##%")
    private BigDecimal borrowRate;
    //订单
    private List<BorrowOrder> borrowOrders;
    @Data
    public static class BorrowOrder{
        //id
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        //币别
        private String borrowCoin;
        //图标
        private String logo;
        //借出数量
        @BigDecimalFormat
        private BigDecimal borrowCapital;
        //质押数量
        @BigDecimalFormat
        private BigDecimal pledgeAmount;
        //利息
        @BigDecimalFormat
        private BigDecimal cumulativeInterest;
        //质押率
        @BigDecimalFormat("#.##%")
        private BigDecimal pledgeRate;

    }
}
