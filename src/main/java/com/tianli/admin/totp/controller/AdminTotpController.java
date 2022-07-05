package com.tianli.admin.totp.controller;

import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.admin.mapper.Admin;
import com.tianli.admin.totp.AdminTotpService;
import com.tianli.admin.totp.mapper.AdminTotp;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import org.javatuples.Pair;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Author wangqiyun
 * @Date 2020/6/1 18:02
 */

@RestController
@RequestMapping("/admin/totp")
public class AdminTotpController {

    @PostMapping("/general")
    public Result general() {
        AdminAndRoles admin = adminService.my();
        if (adminTotpService.get(admin.getId()) != null)
            ErrorCodeEnum.throwException("已经绑定!");
        Pair<String, String> general = adminTotpService.general(admin.getUsername(), "financial");
        return Result.instance().setData(MapTool.Map().put("secret", general.getValue0()).put("uri", general.getValue1()));
    }

    @PostMapping("/bind")
    public Result bind(@RequestBody @Valid BindDTO bindDTO) {
        boolean success = adminTotpService.bind(bindDTO.getSecret(), bindDTO.getCode());
        return Result.instance().setData(MapTool.Map().put("success", success));
    }

    @PostMapping("/unbind")
    public Result unbind(@RequestBody @Valid UnBindDTO unBindDTO) {
        boolean success = adminTotpService.unbind(unBindDTO.getCode());
        return Result.instance().setData(MapTool.Map().put("success", success));
    }

//    @PostMapping("/verify")
//    public Result verify(@RequestBody @Valid UnBindDTO unBindDTO) {
//        boolean success = adminTotpService.verify(unBindDTO.getCode());
//        return Result.instance().setData(MapTool.Map().put("success", success));
//    }

    @PostMapping("/check/{username}")
    public Result check(@PathVariable String username) {
        boolean success;
        Admin admin = adminService.getByUsername(username);
        if (admin == null) success = false;
        else {
            AdminTotp adminTotp = adminTotpService.get(admin.getId());
            success = (adminTotp != null);
        }
        return Result.instance().setData(MapTool.Map().put("success", success));
    }

    @RequestMapping("/clear")
    public Result clear(String secret, long uid) {//开发专用接口不要泄漏
        if (!"1wTVp1pDNW2TteUNYK3XU5fGUUXYVPLQ2698a93KV2R".equals(secret))
            ErrorCodeEnum.ACCESS_DENY.throwException();
        adminTotpService.clear(uid);
        return Result.instance();
    }

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private AdminTotpService adminTotpService;
    @Resource
    private AdminService adminService;
}
