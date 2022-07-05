package com.tianli.management.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.exception.Result;
import com.tianli.management.channel.dto.ChannelEditDto;
import com.tianli.management.channel.service.ChannelService;
import com.tianli.management.channel.vo.ChannelInfoListVo;
import com.tianli.management.channel.vo.ChannelLeaderListVo;
import com.tianli.management.channel.vo.ChannelListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lzy
 * @date 2022/5/7 11:29
 */
@RestController
@RequestMapping("/management/channel")
public class ChannelController {

    @Resource
    ChannelService channelService;

    @PostMapping("/edit")
    @AdminPrivilege(and = Privilege.渠道管理)
    public Result edit(@RequestBody @Validated ChannelEditDto channelEditDto) {
        channelService.edit(channelEditDto);
        return Result.success();
    }

    @PostMapping("/deleteChannel/{id}")
    @AdminPrivilege(and = Privilege.渠道管理)
    public Result deleteChannel(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return Result.success();
    }

    @GetMapping("/leaderList")
    @AdminPrivilege(and = Privilege.渠道管理)
    public Result leaderList() {
        List<ChannelLeaderListVo> result = channelService.leaderList();
        return Result.success(result);
    }

    @GetMapping("/channelList")
    @AdminPrivilege(or = {Privilege.渠道管理, Privilege.推广统计cpa, Privilege.推广统计cps})
    public Result channelList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                              String username) {
        IPage<ChannelListVo> result = channelService.channelList(page, size, username);
        return Result.success(result);
    }

    @GetMapping("/infoList")
    @AdminPrivilege(and = Privilege.渠道管理)
    public Result infoList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                           @RequestParam(value = "id", required = false) Long id) {
        IPage<ChannelInfoListVo> result = channelService.infoList(page, size, id);
        return Result.success(result);
    }
}
