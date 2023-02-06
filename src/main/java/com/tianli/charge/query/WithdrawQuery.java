package com.tianli.charge.query;

import com.tianli.common.blockchain.NetworkType;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author wangqiyun
 * @since 2020/3/31 15:20
 */
@Data
public class WithdrawQuery {

    @NotNull(message = "币别不能为null")
    private String coin;

    @NotNull(message = "提币网络不能为null")
    private NetworkType network;

    @NotNull(message = "地址不能为空")
    private String to;

    private String amount;

}
