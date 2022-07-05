package com.tianli.common;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {
    private Session session;

    public WebSocketClient(URI uri){
        try {
            session = ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println(message);
    }

    public void sendMessage(String str) {
        session.getAsyncRemote().sendText(str);
    }

}
