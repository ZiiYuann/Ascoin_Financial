package com.tianli.captcha.email.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianli.captcha.email.EmailSendStrategy;
import com.tianli.captcha.email.enums.EmailSendEnum;
import com.tianli.common.async.AsyncService;
import com.tianli.management.spot.entity.SGCharge;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TimeZone;

/**
 * @author lzy
 * @date 2022/5/6 16:22
 */
@Service("emailWithdrawalFailedService")
public class EmailWithdrawalFailedService implements EmailSendStrategy {


    @Resource
    EmailAmazonService emailAmazonService;

    @Resource
    AsyncService asyncService;

    @Override
    public void send(String emailAddress, EmailSendEnum emailSendEnum, Long id, Object data) {
        SGCharge sgCharge = BeanUtil.toBean(data,SGCharge.class);
        if (ObjectUtil.isNull(sgCharge)) {
            return;
        }
        String date = new DateTime(TimeZone.getTimeZone("UTC")).toString();
        String title = StrUtil.format(emailSendEnum.getMsgTitle(), date);
        String context = StrUtil.format(emailSendEnum.getMsgContext(), sgCharge.getAmount().toString(), sgCharge.getReason_en());
        asyncService.async(() -> emailAmazonService.send(emailAddress, title, context));
    }
}
