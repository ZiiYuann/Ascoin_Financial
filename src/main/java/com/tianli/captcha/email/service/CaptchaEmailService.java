package com.tianli.captcha.email.service;

import com.google.gson.Gson;
import com.tianli.captcha.phone.mapper.CaptchaPhone;
import com.tianli.captcha.phone.mapper.CaptchaPhoneMapper;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisService;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.MapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.tianli.exception.ErrorCodeEnum.CODE_ERROR;

/**
 * 邮箱发送
 */
@Service
public class CaptchaEmailService {
    public String code(String email, CaptchaPhoneType type) {
        String key = new Gson().toJson(MapTool.Map().put("email", email).put("type", type));
        if (!redisService.expireLock(prefix + "frequent_" + key, 60000L))
            ErrorCodeEnum.TOO_FREQUENT.throwException();
        BoundValueOperations<String, Object> codeOps = redisTemplate.boundValueOps(prefix + "code_" + key);
        Object o = codeOps.get();
        String code;
        if (o == null) {
            CaptchaPhone captchaPhone = new CaptchaPhone();
            captchaPhone.setId(CommonFunction.generalId());
            captchaPhone.setCreate_time(requestInitService.now());
            captchaPhone.setPhone(email);
            captchaPhone.setType(type);
            code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 999999));
            captchaPhone.setCode(code);
            captchaPhone.setRegion("email");
            captchaPhoneMapper.insert(captchaPhone);
            codeOps.set(code, 3600000L, TimeUnit.MILLISECONDS);
            redisService.determineTimesLock(prefix + "times_" + key, 10, 3600000L);
        } else {
            code = o.toString();
        }
        Map<String, String> template = getTemplateByType(type);
        emailAmazonService.send(email, template.get("title"), template.get("context").replaceAll("#code#", code));
        return code;
    }

    private Map<String, String> getTemplateByType(CaptchaPhoneType type) {
        Map<String, String> t = new HashMap<>();
        switch (type) {
            case registration:
                t.put("title", "【Financial】Login Request");
                t.put("context", "Confirm Your Login \n" +
                        "\n" +
                        "Verification code:\n" +
                        "#code#\n" +
                        "The verification code will expire after 30 minutes. Do not share your code with anyone. \n" +
                        "\n" +
                        "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n" +
                        "\n" +
                        "This is an automated message, please do not reply.");
                break;
            case resetLoginPassword:
                t.put("title", "【Financial】Password retrieval Request");
                t.put("context", "Confirm Your Retrieval \n" +
                        "\n" +
                        "Verification code:\n" +
                        "#code#\n" +
                        "The verification code will expire after 30 minutes. Do not share your code with anyone. \n" +
                        "\n" +
                        "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n" +
                        "\n" +
                        "This is an automated message, please do not reply.");
                break;
            case modificationLoginPassword:
                t.put("title", "【Financial】Password Modification Request");
                t.put("context", "Confirm Your Modification \n" +
                        "\n" +
                        "Verification code:\n" +
                        "#code#\n" +
                        "The verification code will expire after 30 minutes. Do not share your code with anyone. \n" +
                        "\n" +
                        "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n" +
                        "\n" +
                        "This is an automated message, please do not reply.");
                break;
            case withdraw:
                t.put("title", "【Financial】Withdrawal Request");
                t.put("context", "Confirm Your Withdrawal \n" +
                        "\n" +
                        "Verification code:\n" +
                        "#code#\n" +
                        "The verification code will expire after 30 minutes. Do not share your code with anyone. \n" +
                        "\n" +
                        "Don’t recognize this activity? Please reset your password and contact customer support immediately. \n" +
                        "\n" +
                        "This is an automated message, please do not reply.");
                break;
            default:
                t.put("title", "");
                t.put("context", "");
                break;
        }
        return t;
    }

    public boolean _verify(String email, CaptchaPhoneType type, String code) {
        if (StringUtils.isEmpty(email) || type == null || StringUtils.isEmpty(code)) return false;
        String key = new Gson().toJson(MapTool.Map().put("email", email).put("type", type));
        BoundValueOperations<String, Object> codeOps = redisTemplate.boundValueOps(prefix + "code_" + key);
        Object o = codeOps.get();
        if (o != null) {
            if (redisService.consumeDetermineTimesLock(prefix + "times_" + key)) {
                return o.toString().equals(code);
            } else {
                redisTemplate.delete(prefix + "code_" + key);
            }
        }
        return false;
    }

    public void verify(String email, CaptchaPhoneType type, String code) {
        if (!this._verify(email, type, code)) CODE_ERROR.throwException();
    }

    public List<CaptchaPhone> selectCaptchaPhone(String phone, Integer page, Integer size) {
        return captchaPhoneMapper.selectCaptchaPhone(phone, page, size);
    }

    public int selectCount(String phone) {
        return captchaPhoneMapper.selectCount(phone);
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    RedisService redisService;
    @Resource
    RequestInitService requestInitService;
    @Resource
    private CaptchaPhoneMapper captchaPhoneMapper;
    @Resource
    private EmailAmazonService emailAmazonService;

    private final String prefix = "captcha_email_";
}
