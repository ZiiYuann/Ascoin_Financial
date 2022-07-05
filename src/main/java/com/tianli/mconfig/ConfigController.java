package com.tianli.mconfig;

import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ConfigController {
    @Resource
    ConfigService configService;

    @GetMapping("/huobi/websocket")
    public Result huobi() {
        return Result.instance().setData(configService.getOrDefault("huobi_websocket_url", "wss://api.huobi.af/ws"));
    }

    @GetMapping("/bian/websocket")
    public Result bian() {
        String bian_websocket_url = configService.getOrDefault("bian_websocket_url", "wss://stream.yshyqxx.com/stream");
        String bian_api_url = configService.getOrDefault("bian_api_url", "api.yshyqxx.com");
        String bian_websocket_url2 = configService.getOrDefault("bian_websocket_url2", "wss://stream.binance.com/stream");
        String bian_api_url2 = configService.getOrDefault("bian_api_url2", "api3.binance.com");
        return Result.success(MapTool.Map()
                .put("bian_websocket_url", bian_websocket_url)
                .put("bian_api_url", bian_api_url)
                .put("bian_websocket_url2", bian_websocket_url2)
                .put("bian_api_url2", bian_api_url2)
        );
    }
}