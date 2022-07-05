package com.tianli.captcha.email;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.captcha.email.enums.EmailSendEnum;
import com.tianli.exception.ErrCodeException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @date 2022/5/6 14:13
 */
@Component
public class EmailSendFactory {


    final Map<String, EmailSendStrategy> emailSendStrategyMap = new ConcurrentHashMap<>();

    public EmailSendFactory(Map<String, EmailSendStrategy> emailSendStrategyMap) {
        emailSendStrategyMap.forEach(this.emailSendStrategyMap::put);
    }

    public EmailSendStrategy getStrategy(String serviceName) {
        EmailSendStrategy emailSendStrategy = emailSendStrategyMap.get(serviceName);
        if (ObjectUtil.isNull(emailSendStrategy)) {
            throw new ErrCodeException("no strategy defined");
        }
        return emailSendStrategy;
    }

    public void send(EmailSendEnum emailSendEnum, Long id, Object data, String emailAddress) {
        getStrategy(emailSendEnum.getServiceName()).send(emailAddress, emailSendEnum, id, data);
    }


}
