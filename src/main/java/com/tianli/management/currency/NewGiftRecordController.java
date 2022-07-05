package com.tianli.management.currency;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.currency.DiscountCurrencyLogService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.DiscountCurrencyLog;
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

/**
 * <p>
 * 用户余额表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@RestController
@RequestMapping("/new/gift")
public class NewGiftRecordController {

    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private DiscountCurrencyLogService discountCurrencyLogService;

    /**
     * 分页接口
     */
    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.新人奖励)
    public Result page(@RequestParam(value = "username", required = false) String username,
                       @RequestParam(value = "startTime", required = false) String startTime,
                       @RequestParam(value = "endTime", required = false) String endTime,
                       @RequestParam(value = "ip", required = false) String ip,
                       @RequestParam(value = "equipment", required = false) String equipment,
                       @RequestParam(value = "grc_result", required = false) Boolean grc_result,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<UserIpLog> dailyGiftRecordPage = userIpLogService.page(new Page<>(page, size),
                new LambdaQueryWrapper<UserIpLog>()
                        .eq(UserIpLog::getBehavior, GrcCheckModular.领取新人福利)
                        .orderByDesc(UserIpLog::getId)
                        .like(StringUtils.isNotBlank(username), UserIpLog::getUsername, username)
                        .eq(Objects.nonNull(grc_result), UserIpLog::getGrc_result, grc_result)
                        .like(StringUtils.isNotBlank(ip), UserIpLog::getIp, ip)
                        .like(StringUtils.isNotBlank(equipment), UserIpLog::getEquipment, equipment)
                        .ge(StringUtils.isNotBlank(startTime), UserIpLog::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), UserIpLog::getCreate_time, endTime)
        );

        List<UserIpLog> records = dailyGiftRecordPage.getRecords();
        long total = dailyGiftRecordPage.getTotal();
        if(total <= 0){
            return Result.success(MapTool.Map().put("count", 0)
                    .put("list", Lists.newArrayList()));
        }
        List<NewGiftRecordVO> voList = records.stream().map(NewGiftRecordVO::convert).collect(Collectors.toList());
        List<Long> ids = records.stream().map(UserIpLog::getBehavior_id).collect(Collectors.toList());
        List<DiscountCurrencyLog> discountCurrencies = discountCurrencyLogService.listByIds(ids);
        Map<Long, DiscountCurrencyLog> discountCurrencyMap = discountCurrencies.stream().collect(Collectors.toMap(DiscountCurrencyLog::getId, Function.identity()));
        List<Long> uids = records.stream().map(UserIpLog::getUid).collect(Collectors.toList());
        List<UserInfo> userInfos = userInfoService.listByIds(uids);
        Map<String, UserInfo>  userInfoMap= userInfos.stream().collect(Collectors.toMap(UserInfo::getUsername, Function.identity()));
        voList.forEach(e -> {
            Long behavior_id = e.getBehavior_id();
            DiscountCurrencyLog discountCurrencyLog = discountCurrencyMap.get(behavior_id);
            if(Objects.nonNull(discountCurrencyLog)){
                e.setAmount(TokenCurrencyType.usdt_omni.money(discountCurrencyLog.getAmount()));
                e.setToken(discountCurrencyLog.getToken());
            }
            String username_ = e.getUsername();
            UserInfo userInfo = userInfoMap.get(username_);
            if(Objects.nonNull(userInfo)){
                e.setNick(userInfo.getNick());
            }
        });
        return Result.success(MapTool.Map().put("count", total)
                .put("list", voList));
    }


}