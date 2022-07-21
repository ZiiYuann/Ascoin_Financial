package com.tianli.borrow.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BorrowCoinMainPageVO {
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
    //借款额度
    private BigDecimal borrowQuota;
    //已借额度比率
    private BigDecimal borrowRate;
    //订单
    private List<BorrowOrder> borrowOrders;
    @Data
    public static class BorrowOrder{
        //币别
        private String borrowCoin;
        //借出数量
        private BigDecimal borrowCapital;
        //质押数量
        private BigDecimal pledgeAmount;
        //利息
        private BigDecimal cumulativeInterest;
        //质押率
        private BigDecimal currentPledgeRate;

    }
}
