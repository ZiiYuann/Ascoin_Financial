package com.tianli.management.captchalist.controller;

import com.google.common.collect.Lists;
import com.tianli.captcha.phone.mapper.CaptchaPhone;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *      验证码列表
 * </P>
 *
 * @author linyifan
 * @since 7/5/21 11:27 AM
 */

@RestController
@RequestMapping("/captcha")
public class CaptchaListController {

    @Resource
    private CaptchaPhoneService captchaPhoneService;

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.验证码列表)
    public Result captchaList(@RequestParam(value = "phone", required = false) String phone,
                              @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                              @RequestParam(value = "size", defaultValue = "10", required = false) Integer size){
        int count = captchaPhoneService.selectCount(phone);
        if(count <= 0){
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        List<CaptchaPhone> captchaPhoneList = captchaPhoneService.selectCaptchaPhone(phone, page, size);
        List<CaptchaListVO> list = captchaPhoneList.stream().map(CaptchaListVO::trans).collect(Collectors.toList());
        return Result.success(MapTool.Map().put("total", count).put("list", list));
    }
}
