package com.tianli.currency_token.webSocketService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket客户端配置类
 */
@Configuration
public class WebSocketClientConfig {

    /**
     * socket连接地址
     */
//    @Value("${com.dl.socket.url}")
    private String webSocketUri = "wss://stream.binance.com:9443/ws/!ticker@arr";

    /**
     * 注入Socket客户端
     * @return
     */
    @Bean
    public CustomizedWebSocketClient initWebSocketClient(){
        URI uri = null;
        try {
            uri = new URI(webSocketUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        CustomizedWebSocketClient webSocketClient = new CustomizedWebSocketClient(uri);
        //启动时创建客户端连接
        webSocketClient.connect();
        return webSocketClient;
    }

}
