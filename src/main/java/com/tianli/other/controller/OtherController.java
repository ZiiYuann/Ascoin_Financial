package com.tianli.other.controller;

import com.tianli.exception.Result;
import com.tianli.other.service.BannerService;
import com.tianli.tool.IPUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;

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

    @GetMapping("/ip")
    public Result ip(HttpServletRequest request) {
        try {
            var mapOptional = Optional.ofNullable(IPUtils.ipAnalysis(IPUtils.getIpAddress(request)));
            var result = mapOptional.orElse(Collections.emptyMap());
            return Result.success(result);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(Collections.emptyMap());
        }

    }

}
