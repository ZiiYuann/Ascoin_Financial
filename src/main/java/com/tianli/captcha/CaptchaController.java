package com.tianli.captcha;

import com.tianli.tool.captcha.GifCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author wangqiyun
 * @Date 2018/10/16 下午3:55
 */
@RestController
@RequestMapping("/captcha")
@Slf4j
public class CaptchaController {

    @Resource
    CaptchaService captchaService;

    @GetMapping("/code")
    public void code(@Range(min = 1L, max = 1024L) int width, @Range(min = 1L, max = 1024L) int height, HttpServletResponse httpServletResponse) {
        GifCaptcha captcha = new GifCaptcha(width, height, 4);
        String text = captcha.text();
        captchaService.code(text);
        try {
            httpServletResponse.setContentType("image/gif");
            captcha.out(httpServletResponse.getOutputStream());
        } catch (IOException e) {
            // do nothing
            log.error("获取code异常 ", e);
        }
    }


}
