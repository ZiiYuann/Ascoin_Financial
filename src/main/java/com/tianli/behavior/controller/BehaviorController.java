package com.tianli.behavior.controller;

import com.tianli.behavior.dto.BehaviorAddDto;
import com.tianli.behavior.service.BehaviorService;
import com.tianli.common.async.AsyncService;
import com.tianli.exception.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lzy
 * @date 2022/5/16 15:36
 */
@RestController
@RequestMapping("/behavior")
@Slf4j
public class BehaviorController {

    @Resource
    BehaviorService behaviorService;

    @Resource
    private AsyncService asyncService;


    @PostMapping("/add")
    public Result add(@RequestBody BehaviorAddDto behaviorAddDto) {
        asyncService.async(() -> behaviorService.add(behaviorAddDto));
        return Result.success();
    }
}
