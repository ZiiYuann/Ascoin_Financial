package com.tianli.management.currency;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.currency.DailyGiftRecordService;
import com.tianli.currency.mapper.DailyGiftRecord;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.logs.mapper.UserIpLog;
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
@RequestMapping("/daily/gift")
public class DailyGiftRecordController {

    @Resource
    private DailyGiftRecordService dailyGiftRecordService;
    @Resource
    private UserIpLogService userIpLogService;

    /**
     * 分页接口
     */
    @GetMapping("/old/page")
    @AdminPrivilege(and = Privilege.每日奖励)
    public Result page(@RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "startTime", required = false) String startTime,
                        @RequestParam(value = "endTime", required = false) String endTime,
                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<DailyGiftRecord> dailyGiftRecordPage = dailyGiftRecordService.page(new Page<>(page, size),
                new LambdaQueryWrapper<DailyGiftRecord>()
                        .orderByDesc(DailyGiftRecord::getId)
                        .like(StringUtils.isNotBlank(username), DailyGiftRecord::getUsername, username)
                        .ge(StringUtils.isNotBlank(startTime), DailyGiftRecord::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), DailyGiftRecord::getCreate_time, endTime));
        List<DailyGiftRecord> records = dailyGiftRecordPage.getRecords();
        long total = dailyGiftRecordPage.getTotal();
        List<DailyGiftRecordVO> voList = records.stream().map(DailyGiftRecordVO::convert).collect(Collectors.toList());
        if (total > 0) {
            List<Long> chargeIdList = voList.stream().map(DailyGiftRecordVO::getId).collect(Collectors.toList());
            List<UserIpLog> list = userIpLogService.list(Wrappers.lambdaQuery(UserIpLog.class)
                    .eq(UserIpLog::getBehavior, GrcCheckModular.领取每日福利)
                    .in(UserIpLog::getBehavior_id, chargeIdList));
            Map<Long, UserIpLog> longUserIpLogMap = list.stream().collect(Collectors.toMap(UserIpLog::getBehavior_id, Function.identity(), (a, b) -> a));
            voList.forEach(e -> {
                UserIpLog userIpLog = longUserIpLogMap.get(e.getId());
                if (Objects.isNull(userIpLog)) {
                    return;
                }
                e.fillOtherProperties(userIpLog);
            });
        }
        return Result.success(MapTool.Map().put("count", total)
                .put("list", voList));
    }

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.每日奖励)
    public Result pageNew(@RequestParam(value = "username", required = false) String username,
                       @RequestParam(value = "startTime", required = false) String startTime,
                       @RequestParam(value = "endTime", required = false) String endTime,
                       @RequestParam(value = "ip", required = false) String ip,
                       @RequestParam(value = "equipment", required = false) String equipment,
                       @RequestParam(value = "grc_result", required = false) Boolean grc_result,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<UserIpLog> logPage = userIpLogService.page(new Page<>(page, size),
                Wrappers.lambdaQuery(UserIpLog.class)
                        .eq(UserIpLog::getBehavior, GrcCheckModular.领取每日福利)
                        .like(StringUtils.isNotBlank(username), UserIpLog::getUsername, username)
                        .like(StringUtils.isNotBlank(ip), UserIpLog::getIp, ip)
                        .like(StringUtils.isNotBlank(equipment), UserIpLog::getEquipment, equipment)
                        .eq(Objects.nonNull(grc_result), UserIpLog::getGrc_result, grc_result)
                        .ge(StringUtils.isNotBlank(startTime), UserIpLog::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), UserIpLog::getCreate_time, endTime)
        );
        long total = logPage.getTotal();
        if(total <= 0){
            return Result.success(MapTool.Map().put("count", 0)
                    .put("list", Lists.newArrayList()));
        }
        List<UserIpLog> logList = logPage.getRecords();
        List<Long> ids = logList.stream().map(UserIpLog::getBehavior_id).collect(Collectors.toList());
        List<DailyGiftRecord> recordList = dailyGiftRecordService.list(new LambdaQueryWrapper<DailyGiftRecord>()
                .in(DailyGiftRecord::getId, ids));
        List<DailyGiftRecordVO> voList = logList.stream().map(DailyGiftRecordVO::convert).collect(Collectors.toList());
        {
            Map<Long, DailyGiftRecord> recordMap = recordList.stream().collect(Collectors.toMap(DailyGiftRecord::getId, Function.identity()));
            voList.forEach(e -> {
                DailyGiftRecord dailyGiftRecord = recordMap.get(e.getBehavior_id());
                if(Objects.nonNull(dailyGiftRecord)){
                    e.setAmount(dailyGiftRecord.getToken().money(dailyGiftRecord.getAmount()));
                    e.setToken(dailyGiftRecord.getToken());
                }
            });
        }
        return Result.success(MapTool.Map().put("count", total)
                .put("list", voList));
    }


}