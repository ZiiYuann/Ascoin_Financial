package com.tianli.exchange.handler;

import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.service.IKLinesInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzy
 * @date 2022/6/10 13:59
 */
@Component
public class MySqlHandler implements MarketHandler {

    @Resource
    IKLinesInfoService ikLinesInfoService;


    @Override
    public void handleKLine(KLinesInfo kLine) {
        ikLinesInfoService.save(kLine);
    }
}
