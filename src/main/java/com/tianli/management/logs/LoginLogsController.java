package com.tianli.management.logs;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.logs.mapper.UserIpLog;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/login/logs")
public class LoginLogsController {

    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.登录记录)
    public Result page(LoginLogsPageReq req){
        Page<UserIpLog> page = userIpLogService.page(new Page<>(req.getPage(), req.getSize()),
                Wrappers.lambdaQuery(UserIpLog.class)
                        .in(UserIpLog::getBehavior, Lists.newArrayList(GrcCheckModular.密码登录, GrcCheckModular.验证码登录))
                        .eq(Objects.nonNull(req.getGrc_result()), UserIpLog::getGrc_result, req.getGrc_result())
                        .like(StringUtils.isNotBlank(req.getUsername()), UserIpLog::getUsername, req.getUsername())
                        .like(StringUtils.isNotBlank(req.getIp()), UserIpLog::getIp, req.getIp())
                        .like(StringUtils.isNotBlank(req.getEquipment_type()), UserIpLog::getEquipment_type, req.getEquipment_type())
                        .like(StringUtils.isNotBlank(req.getEquipment()), UserIpLog::getEquipment, req.getEquipment())
                        .ge(StringUtils.isNotBlank(req.getStartTime()), UserIpLog::getCreate_time, req.getStartTime())
                        .le(StringUtils.isNotBlank(req.getEndTime()), UserIpLog::getCreate_time, req.getEndTime())
        );
        List<UserIpLog> records = page.getRecords();
        List<String> usernameList = records.stream().map(UserIpLog::getUsername).collect(Collectors.toList());
        Map<String, UserInfo> userMap;
        if(!CollectionUtils.isEmpty(usernameList)){
            List<UserInfo> userInfos = userInfoService.list(Wrappers.lambdaQuery(UserInfo.class).in(UserInfo::getUsername, usernameList));
            userMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUsername, Function.identity(), (a, b) -> a));
        }else{
            userMap = Maps.newHashMap();
        }
        List<UserLoginVO> vos = records.stream().map(e -> UserLoginVO.convert(e, userMap.get(e.getUsername()))).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map()
                .put("total", page.getTotal())
                .put("list", vos));
    }

}
