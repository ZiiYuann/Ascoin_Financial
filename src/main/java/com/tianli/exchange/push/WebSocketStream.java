package com.tianli.exchange.push;

import lombok.Builder;

/**
 * @author lzy
 * @date 2022/6/15 15:50
 */
@Builder
public class WebSocketStream {

    public String stream;

    private Object data;
}
