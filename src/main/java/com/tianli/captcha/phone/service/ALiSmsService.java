package com.tianli.captcha.phone.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lzy
 * @date 2022/4/20 5:11 下午
 */
@Service
@Slf4j
public class ALiSmsService {

    @Resource
    ConfigService configService;

    public void sendSms(String phone, String text) {
        String account = configService._get("ali_sms_account");
        String password = configService._get("ali_sms_password");

        if (StrUtil.isNotBlank(account) && StrUtil.isNotBlank(password)) {
            MapTool data = MapTool.Map()
                    .put("userId", account)
                    .put("account", account)
                    .put("password", password)
                    .put("mobile", phone)
                    .put("content", text)
                    .put("sendTime", "")
                    .put("action", "sendhy");
            String str = JSONUtil.toJsonStr(JSONUtil.parse(data));
            log.info("短线请求参数:{}", str);
            String post = HttpUtil.post("http://smsapi.5taogame.com/sms/httpSmsInterface2", str);
            log.info("短信发送接口返回参数:{}", post);
        }
    }

}
