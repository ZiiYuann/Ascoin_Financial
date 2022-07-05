package com.tianli.management.fundmanagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.Result;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("rebateManage")
public class RebateManageController {

    @Resource
    private RebateService rebateService;

    @Resource
    private UserInfoService userInfoService;

    @GetMapping("page")
    @AdminPrivilege(and = Privilege.返佣记录)
    public Result page(String phone, String startTime, String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        ;
        LambdaQueryWrapper<Rebate> queryWrapper = new LambdaQueryWrapper<Rebate>()
                .orderByDesc(Rebate::getId)
                .ge(StringUtils.isNotBlank(startTime), Rebate::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Rebate::getCreate_time, endTime);
        List<UserInfo> list = userInfoService.list(new LambdaQueryWrapper<UserInfo>().like(UserInfo::getUsername, phone));
        if(StringUtils.isNotBlank(phone)){
            List<Long> uidList = list.stream().map(UserInfo::getId).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(uidList)){
                return Result.instance().setData(MapTool.Map()
                        .put("total", 0)
                        .put("list", Lists.newArrayList())
                );
            }
            queryWrapper.in(StringUtils.isNotBlank(phone), Rebate::getRebate_uid, uidList);
        }
        Page<Rebate> rebatePage = rebateService.page(new Page<>(page, size), queryWrapper);
        List<Rebate> records = rebatePage.getRecords();
        if(CollectionUtils.isEmpty(records)){
            return Result.instance().setData(MapTool.Map()
                    .put("total", rebatePage.getTotal())
                    .put("list", Lists.newArrayList())
            );
        }
        List<Long> ids = records.stream().map(Rebate::getRebate_uid).collect(Collectors.toList());
        List<UserInfo> userInfos = userInfoService.listByIds(ids);
        Map<Long, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getId, Function.identity(), (a, b) -> a));

        List<RebateManageVO> vos = records.stream().map(e -> {
            UserInfo userInfo = userInfoMap.get(e.getRebate_uid());
            double money;
            if (CurrencyTokenEnum.usdt_omni.equals(e.getToken())) {
                money = TokenCurrencyType.usdt_omni.money(e.getRebate_amount());
            } else {
                money = e.getToken().money(e.getRebate_amount());
            }
            return RebateManageVO.builder()
                    .id(e.getId())
                    .create_time(e.getCreate_time())
                    .rebate_amount(money)
                    .rebate_uid(e.getRebate_uid())
                    .rebate_uid_nick(Objects.nonNull(userInfo) ? userInfo.getNick() : "")
                    .rebate_uid_phone(Objects.nonNull(userInfo) ? userInfo.getUsername() : "")
                    .token(e.getToken()).build();
        }).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map()
                .put("total", rebatePage.getTotal())
                .put("list", vos)
        );
    }
}
