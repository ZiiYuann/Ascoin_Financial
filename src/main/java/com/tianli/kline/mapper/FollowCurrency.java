package com.tianli.kline.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 关注的火币平台币种表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class FollowCurrency {

    /**
     * 币种名 ETH/USDT BTC/USDT...
     */
    private String name;

    /**
     * k线参数符号
     */
    private String symbol;


    /**
     * 是否选中 0:未选中  1:选中
     */
    private Boolean selected;

    /**
     * icon 图片地址
     */
    private String img;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 赢钱占比
     */
    private Double win_rate;

}
