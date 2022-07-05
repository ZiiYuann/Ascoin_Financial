package com.tianli.exchange.handler;

import com.tianli.exchange.entity.KLinesInfo;

/**
 * @author lzy
 * @date 2022/6/10 13:58
 */
public interface MarketHandler {

    /**
     * K线信息处理
     *
     * @param kLine
     */
    void handleKLine(KLinesInfo kLine);
}
