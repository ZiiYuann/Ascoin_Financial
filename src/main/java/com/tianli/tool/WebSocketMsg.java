package com.tianli.tool;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lzy
 * @date 2022/4/8 2:34 下午
 */
@Builder
@Data
public class WebSocketMsg implements Serializable {
    /**
     * 消息推送类型
     */
    private String type;

    private Object data;

    public static WebSocketMsg getWebSocketMsg(WebSocketMsgTypeEnum webSocketMsgTypeEnum, Object object) {
        return WebSocketMsg.builder().type(webSocketMsgTypeEnum.name()).data(object).build();
    }
}
