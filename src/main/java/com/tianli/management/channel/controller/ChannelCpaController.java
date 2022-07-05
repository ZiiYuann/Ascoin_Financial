package com.tianli.management.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.exception.Result;
import com.tianli.management.channel.service.ChannelUserService;
import com.tianli.management.channel.vo.ChannelCpaListVo;
import com.tianli.management.channel.vo.ChannelCpaStatisticsVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lzy
 * @date 2022/5/7 16:40
 */
@RestController
@RequestMapping("/management/channel/cpa")
public class ChannelCpaController {

    @Resource
    ChannelUserService channelUserService;


    @GetMapping("/cpaList")
    @AdminPrivilege(and = Privilege.推广统计cpa)
    public Result cpaList(Long channelId, String username, Integer kycStatus, String startTime, String endTime,
                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                          @RequestParam(value = "size", defaultValue = "10") Integer size) {
        IPage<ChannelCpaListVo> result = channelUserService.cpaList(channelId, username, kycStatus, startTime, endTime, page, size);
        return Result.success(result);
    }

    @GetMapping("/export")
    @AdminPrivilege(and = Privilege.推广统计cpa)
    public void export(Long channelId, String username, Integer kycStatus, String startTime, String endTime) {
        channelUserService.export(channelId, username, kycStatus, startTime, endTime);
    }


    @GetMapping("/statistics")
    @AdminPrivilege(and = Privilege.推广统计cpa)
    public Result statistics(Long channelId, String username, Integer kycStatus, String startTime, String endTime) {
        ChannelCpaStatisticsVo channelCpaStatisticsVo = channelUserService.statistics(channelId, username, kycStatus, startTime, endTime);
        return Result.success(channelCpaStatisticsVo);
    }
}
