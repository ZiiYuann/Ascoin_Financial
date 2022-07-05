package com.tianli.captcha.email.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.tianli.captcha.email.EmailSendStrategy;
import com.tianli.captcha.email.enums.EmailSendEnum;
import com.tianli.common.async.AsyncService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TimeZone;

/**
 * @author lzy
 * @date 2022/5/6 14:19
 */
@Service("ipWarnService")
public class EmailIpService implements EmailSendStrategy {

    @Resource
    EmailAmazonService emailAmazonService;

    @Resource
    AsyncService asyncService;


    @Override
    public void send(String emailAddress, EmailSendEnum emailSendEnum, Long id, Object data) {
        String ip = data.toString();
        String date = new DateTime(TimeZone.getTimeZone("UTC")).toString();
        String title = StrUtil.format(emailSendEnum.getMsgTitle(), ip, date);
        String context = StrUtil.format(emailSendEnum.getMsgContext(), emailAddress, date, ip);
        asyncService.async(() -> emailAmazonService.send(emailAddress, title, context));
    }

}
