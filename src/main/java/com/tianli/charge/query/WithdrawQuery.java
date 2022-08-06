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

    @NotNull(message = "提币网络不能为null")
    private NetworkType network;

    @NotNull(message = "币别不能为null")
    private CurrencyCoin coin;

    @NotNull(message = "地址不能为空")
    private String to;

}
