package com.tianli.currency_token.webSocketService;

import com.tianli.common.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

@Component
@Slf4j
public class WebSocketLiveSchedule {

//    private CustomizedWebSocketClient webSocketClient = new WebSocketClientConfig().initWebSocketClient();
    @Resource
    private CustomizedWebSocketClient customizedWebSocketClient;

    @Scheduled(fixedDelay = 1000 * 10)
    public void live() {
        if(!customizedWebSocketClient.isOpen()){
            if(customizedWebSocketClient.getReadyState().equals(WebSocket.READYSTATE.NOT_YET_CONNECTED)){
                customizedWebSocketClient.connect();
            }else if(customizedWebSocketClient.getReadyState().equals(WebSocket.READYSTATE.CLOSED)){
                customizedWebSocketClient.reconnect();
            }
        }
    }

}
