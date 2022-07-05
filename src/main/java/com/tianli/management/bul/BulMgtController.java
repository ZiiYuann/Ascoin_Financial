package com.tianli.management.bul;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.blocklist.BlackUserListService;
import com.tianli.blocklist.mapper.BlackUserList;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mgt/bul")
public class BulMgtController {
    @Resource
    private BlackUserListService blackUserListService;
    @Resource
    private UserService userService;
    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.强杀管理)
    public Result page(BulMgtPageReq req) {
        Page<BlackUserList> page = blackUserListService.page(new Page<>(req.getPage(), req.getSize()),
                Wrappers.lambdaQuery(BlackUserList.class)
                        .like(StringUtils.isNotBlank(req.getUsername()), BlackUserList::getUsername, req.getUsername())
                        .orderByDesc(BlackUserList::getId)
        );
        List<BlackUserList> records = page.getRecords();
        List<BulMgtPageVO> vos = records.stream().map(BulMgtPageVO::convert).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map()
                .put("total", page.getTotal())
                .put("list", vos));
    }

    @PostMapping("/add")
    @AdminPrivilege(and = {Privilege.强杀管理})
    public Result info(@RequestBody BulAddReq req){
        AdminInfo adminInfo = AdminContent.get();
        User user = userService._getByUsername(req.getUsername());
        if (Objects.isNull(user)) ErrorCodeEnum.USER_NOT_EXIST.throwException();
        UserInfo userInfo = userInfoService.getOrSaveById(user.getId());
        LocalDateTime now = LocalDateTime.now();
        blackUserListService.save(BlackUserList.builder()
                        .id(CommonFunction.generalId())
                        .create_time(now)
                        .update_time(now)
                        .uid(user.getId())
                        .username(user.getUsername())
                        .nick(userInfo.getNick())
                        .opt_admin(adminInfo.getUsername())
                        .opt_admin_id(adminInfo.getAid())
                        .node(req.getNode())
                        .control(req.getControl())
                .build());
        return Result.success();
    }

    @DeleteMapping("/del/{id}")
    @AdminPrivilege(and = {Privilege.强杀管理})
    public Result audit(@PathVariable("id") Long id){
        BlackUserList userList = blackUserListService.getById(id);
        if (Objects.isNull(userList)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        blackUserListService.removeById(id);
        return Result.success();
    }


}

