package com.tianli.bet.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.common.async.AsyncService;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.http.JSoupUtils;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BianWSHostTask {

    private static final String LOCK_KEY = "Crawl:BianWSHost:Lock";


    @Resource
    private AsyncService asyncService;

    /**
     * 获取币安runtimeConfig
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void crawlWsHost() {
        asyncService.async(() -> {
            BoundValueOperations<String, Object> objectBoundValueOperations = redisTemplate.boundValueOps(LOCK_KEY);
            Boolean lock = objectBoundValueOperations.setIfAbsent(UUID.randomUUID(), 1L, TimeUnit.MINUTES);
            if(Objects.isNull(lock) || !lock){
                return;
            }
            Document document = null;
            try {
                document = JSoupUtils.get("https://www.binancezh.top/zh-CN/trade/BNB_BUSD");
            } catch (Exception e) {
                log.warn("抓取[https://www.binancezh.top/zh-CN/trade/BNB_BUSD]失败");
            }
            if(Objects.isNull(document)){
                log.warn("抓取[https://www.binancezh.top/zh-CN/trade/BNB_BUSD]页面, 获取document is null");
                return;
            }
            Element body = document.body();
            if(Objects.isNull(body)){
                log.warn("抓取[https://www.binancezh.top/zh-CN/trade/BNB_BUSD]页面, 获取document.body is null");
                return;
            }
            Elements select = body.select("#__APP_DATA");
            if(Objects.isNull(select) || select.size() <= 0){
                log.warn("抓取[https://www.binancezh.top/zh-CN/trade/BNB_BUSD]页面, 获取document.body.runtimeConfig is null");
                return;
            }
            String html = select.get(0).html();
            JsonObject jsonObject = new Gson().fromJson(html, JsonObject.class);
            String ws_host = JsonObjectTool.getAsString(jsonObject, "runtimeConfig.WS_HOST");
            if(StringUtils.isBlank(ws_host)){
                log.warn("抓取[https://www.binancezh.top/zh-CN/trade/BNB_BUSD]页面, 解析document.body.runtimeConfig属性中的WS_HOST为空");
                return;
            }
            ws_host = ws_host.replace(":443", "/stream");
            configService.replace("bian_websocket_url", ws_host);
            redisTemplate.delete(LOCK_KEY);
        });
    }

    @Resource
    private ConfigService configService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
}
