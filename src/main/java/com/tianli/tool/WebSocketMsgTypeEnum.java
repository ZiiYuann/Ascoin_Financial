package com.tianli.tool;

import lombok.Getter;

/**
 * @author lzy
 * @date 2022/4/8 2:42 下午
 */
@Getter
public enum WebSocketMsgTypeEnum {
    /**
     * 订单结算
     */
    order_settlement,
    /**
     * 机器人刷新
     */
    robotBet_refresh,

    kline_push,
    ticker24Hr,
    trade_push,

    depth_push


}
