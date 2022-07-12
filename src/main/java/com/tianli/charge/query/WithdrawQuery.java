package com.tianli.charge.query;

import com.tianli.common.blockchain.CurrencyNetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author  wangqiyun
 * @since  2020/3/31 15:20
 */
@Data
public class WithdrawQuery {

    @NotNull(message = "币种不能为空")
    private CurrencyAdaptType currencyAdaptType;

    @DecimalMin(value = "0.0001", message = "提现金额不能为空")
    private double amount;

    @NotBlank(message = "接收地址不能为空")
    private String address;

    /**
     * 提币网络
     */
    private CurrencyNetworkType currencyNetworkType;


}
