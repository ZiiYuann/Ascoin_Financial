package com.tianli.user.userinfo.controller;


import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController {

    @PostMapping("/update")
    public Result update(@RequestBody UserInfoDTO userInfoDTO){
        Long uid = requestInitService.uid();
        UserInfo userInfo = UserInfo.builder()
                .id(uid).build();
        if (Objects.nonNull(userInfoDTO.getNick())){
            userInfo.setNick(userInfoDTO.getNick());
        }
        if (Objects.nonNull(userInfoDTO.getAvatar())){
            userInfo.setAvatar(userInfoDTO.getAvatar());
        }
        boolean update = userInfoService.updateById(userInfo);
        if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.instance();
    }

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private UserInfoService userInfoService;

}

