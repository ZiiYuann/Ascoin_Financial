package com.tianli.agent.management.controller;

import cn.hutool.core.util.IdUtil;
import com.tianli.agent.management.vo.CaptchaImageVO;
import com.tianli.common.RedisConstants;
import com.tianli.exception.Result;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 */
@RestController
@RequestMapping("/agent/management")
public class CaptchaController {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public Result getCode() throws IOException {
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = RedisConstants.CAPTCHA_CODE_KEY + uuid;

        String code;
        Captcha captcha = new SpecCaptcha(115, 42);

        // 生成验证码
        code = captcha.text();
        RBucket<Object> bucket = redissonClient.getBucket(verifyKey);
        bucket.set(code, 5L, TimeUnit.MINUTES);

        CaptchaImageVO captchaImageVO = CaptchaImageVO.builder()
                .uuid(uuid)
                .img(captcha.toBase64())
                .build();
        return Result.success(captchaImageVO);
    }
}
