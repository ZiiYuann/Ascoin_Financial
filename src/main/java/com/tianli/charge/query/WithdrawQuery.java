package com.tianli.charge.query;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * @author  wangqiyun
 * @since  2020/3/31 15:20
 */
@Data
public class WithdrawQuery {

    @DecimalMin(value = "0.0001", message = "提现金额不能为空")
    private double amount;

    /**
     * 提币网络
     */
    @NotNull
    private NetworkType network;

    @NotNull
    private CurrencyCoin coin;

    @NotNull
    private String to;

}
