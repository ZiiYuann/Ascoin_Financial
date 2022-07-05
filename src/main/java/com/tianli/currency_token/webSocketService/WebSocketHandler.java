package com.tianli.currency_token.webSocketService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.tianli.tool.BianPriceCache;
import org.springframework.stereotype.Service;

@Service
public class WebSocketHandler {

    public void handlerMessage(String message) {
        JsonArray ja = new Gson().fromJson(message, JsonArray.class);
        for(JsonElement je: ja) {
            Double price = je.getAsJsonObject().get("c").getAsDouble();
            String symbol = je.getAsJsonObject().get("s").getAsString();
            BianPriceCache.setLatestBianPrice(symbol, price);
        }
    }
}
