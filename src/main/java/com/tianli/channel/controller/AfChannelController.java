package com.tianli.channel.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.tianli.channel.service.AfChannelService;
import com.tianli.common.async.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author lzy
 * @date 2022/5/20 16:19
 */
@RestController
@RequestMapping("/af")
@Slf4j
public class AfChannelController {

    @Resource
    HttpServletRequest request;

    @Resource
    AfChannelService afChannelService;

    @Resource
    AsyncService asyncService;


    @PostMapping("/callback")
    public void callback() throws IOException {
        String data = HttpUtil.getString(request.getInputStream(), Charset.defaultCharset(), false);
        log.info("回调数据:{}", data);
        if (StrUtil.isBlank(data)) {
            return;
        }
        asyncService.async(() -> afChannelService.add(data));
    }

}
