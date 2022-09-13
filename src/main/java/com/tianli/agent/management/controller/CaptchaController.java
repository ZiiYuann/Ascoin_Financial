package com.tianli.agent.management.controller;

import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.Producer;
import com.tianli.agent.management.vo.CaptchaImageVO;
import com.tianli.common.RedisConstants;
import com.tianli.exception.Result;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 */
@RestController
@RequestMapping("/agent/management")
public class CaptchaController {
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

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

        String capStr, code;
        BufferedImage image;
        Captcha captcha = new SpecCaptcha(115, 42);

        // 生成验证码
        capStr = code = captcha.text();
        image = captchaProducer.createImage(capStr);
        RBucket<Object> bucket = redissonClient.getBucket(verifyKey);
        bucket.set(code, 5L, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
        CaptchaImageVO captchaImageVO = CaptchaImageVO.builder()
                .uuid(uuid)
                .img(captcha.toBase64())
                .build();
        return Result.success(captchaImageVO);
    }
}
