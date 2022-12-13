package com.tianli.management.controller;

import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.other.entity.Banner;
import com.tianli.other.query.BannerIoUQuery;
import com.tianli.other.query.MBannerListQuery;
import com.tianli.other.service.BannerService;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-12
 **/
@RestController
@RequestMapping("/management/other")
public class ManageOtherController {

    @Resource
    private BannerService bannerService;

    @GetMapping("/banner/list")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result bannerList(PageQuery<Banner> pageQuery, MBannerListQuery query) {
        return Result.success(bannerService.MList(pageQuery.page(), query));
    }

    @PostMapping("/banner")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result bannerSave(@RequestBody @Valid BannerIoUQuery query) {
        bannerService.saveOrUpdate(query);
        return Result.success();
    }
}
