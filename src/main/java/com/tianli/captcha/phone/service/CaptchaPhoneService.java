package com.tianli.captcha.phone.service;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.tianli.captcha.phone.mapper.CaptchaPhone;
import com.tianli.captcha.phone.mapper.CaptchaPhoneMapper;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisService;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
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
 * @Author wangqiyun
 * @Date 2018/12/5 5:50 PM
 */
@Service
public class CaptchaPhoneService {
    public String code(String phone, CaptchaPhoneType type, String regionNo) {
        String key = new Gson().toJson(MapTool.Map().put("phone", phone).put("type", type));
        if (!redisService.expireLock(prefix + "code_lock" + key, 60000L))
            ErrorCodeEnum.TOO_FREQUENT.throwException();
        BoundValueOperations<String, Object> codeOps = redisTemplate.boundValueOps(prefix + "code_" + key);
        Object o = codeOps.get();
        String code;
        if (o == null) {
            code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 999999));
            CaptchaPhone captchaPhone = new CaptchaPhone();
            captchaPhone.setId(CommonFunction.generalId());
            captchaPhone.setCreate_time(requestInitService.now());
            captchaPhone.setPhone(phone);
            captchaPhone.setType(type);
            captchaPhone.setCode(code);
            captchaPhone.setRegion(regionNo);
            captchaPhoneMapper.insert(captchaPhone);
            codeOps.set(code, 3600000L, TimeUnit.MILLISECONDS);
            redisService.determineTimesLock(prefix + "times_" + key, 10, 3600000L);
        } else
            code = o.toString();

        String text;
        if (StrUtil.equals("86", regionNo)) {
            text = configService.get("text");
            aLiSmsService.sendSms(phone, StrUtil.format(text, code));
        } else {
            phone = "+" + regionNo + phone;
            text = configService._get("en_text");
            cmSMSService.sendSms(phone, StrUtil.format(text, code));
        }
        return code;
    }

    public boolean _verify(String phone, CaptchaPhoneType type, String code) {
        if (StringUtils.isEmpty(phone) || type == null || StringUtils.isEmpty(code)) return false;
        String key = new Gson().toJson(MapTool.Map().put("phone", phone).put("type", type));
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

    public void verify(String phone, CaptchaPhoneType type, String code) {
        if (!this._verify(phone, type, code)) CODE_ERROR.throwException();
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
    private YunpianRequestService yunpianRequestService;

    @Resource
    ALiSmsService aLiSmsService;

    @Resource
    CmSMSService cmSMSService;
    @Resource
    private ConfigService configService;

    private final String prefix = "captcha_phone_";
    private static final Map<String, String> REGION_NO_YUNPIAN_MAP = new HashMap<>();

    static {
        REGION_NO_YUNPIAN_MAP.put("86", "yunpian_text");
    }

    public String getRegion(String phone) {
        return captchaPhoneMapper.selectRegion(phone);
    }
}
