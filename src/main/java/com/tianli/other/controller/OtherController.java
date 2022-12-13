package com.tianli.other.controller;

import com.tianli.exception.Result;
import com.tianli.other.service.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-12
 **/
@RestController
@RequestMapping("/other")
public class OtherController {

    @Resource
    private BannerService bannerService;

    @GetMapping("/banner/list")
    public Result bannerList() {
        return Result.success(bannerService.processList());
    }

}
