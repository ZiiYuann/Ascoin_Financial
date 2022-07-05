package com.tianli.captcha.email;

import com.tianli.captcha.email.enums.EmailSendEnum;

/**
 * @author lzy
 * @date 2022/5/6 14:12
 */
public interface EmailSendStrategy {

    void send(String emailAddress, EmailSendEnum emailSendEnum, Long id, Object data);
}
