package com.tianli.exception;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/errMmp")
public class ErrMsgMappingController {

    @Resource
    private ErrMsgMappingService errMsgMappingService;

    @PostMapping("/type/{type}")
    public Result putNewMmp(@PathVariable("type") String type,
                            String key,
                            String val,
                            String password){
        if(StringUtils.isBlank(key) || StringUtils.isBlank(val)){
            return Result.success();
        }
        String password_ = "tspeculate_mmp";
        if(StringUtils.isBlank(password) || !StringUtils.equals(password, password_)){
            return Result.success();
        }
        errMsgMappingService.putThaiMsg(type, key, val);
        return Result.success();
    }
}
