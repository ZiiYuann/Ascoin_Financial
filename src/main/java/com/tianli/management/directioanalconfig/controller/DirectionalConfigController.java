package com.tianli.management.directioanalconfig.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kline.FollowCurrencyService;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.management.directioanalconfig.dto.CreateDirectionalConfigDTO;
import com.tianli.management.directioanalconfig.dto.DirectionalConfigStatus;
import com.tianli.management.directioanalconfig.dto.UpdateDirectionConfigDTO;
import com.tianli.management.directioanalconfig.mapper.DirectionalConfig;
import com.tianli.management.directioanalconfig.service.DirectionalConfigService;
import com.tianli.management.directioanalconfig.vo.DirectionalConfigVO;
import com.tianli.management.directioanalconfig.vo.StatVO;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chensong
 *  2021-03-04 11:04
 * @since 1.0.0
 */
@RestController
@RequestMapping("directional/config")
public class DirectionalConfigController {

    @Resource
    private DirectionalConfigService directionalConfigService;
    @Resource
    private FollowCurrencyService followCurrencyService;
//    @Resource
//    private AdminBusinessService adminBusinessService;
    @Resource
    private UserInfoService userInfoService;

    @GetMapping("getCurrencyType")
    public Result getCurrencyType(){
        List<FollowCurrency> list = followCurrencyService.list(
                new LambdaQueryWrapper<FollowCurrency>().orderByAsc(FollowCurrency::getSort));
        List<String> collect = list.stream().map(FollowCurrency::getName).collect(Collectors.toList());
        return Result.success(collect);
    }

    @PostMapping("create")
    @AdminPrivilege(and = Privilege.定向配置)
    public Result create(@RequestBody @Valid CreateDirectionalConfigDTO dto){
        directionalConfigService.create(dto);
        return Result.success();
    }

//    @PostMapping("update")
//    @AdminPrivilege(and = Privilege.定向配置, identity = 1)
    public Result update(@RequestBody UpdateDirectionConfigDTO dto){
        boolean update = directionalConfigService.update(new LambdaUpdateWrapper<DirectionalConfig>()
                .eq(DirectionalConfig::getId, dto.getId())
                .set(DirectionalConfig::getCurrency_type, dto.getCurrency_type())
                .set(DirectionalConfig::getResult, dto.getResult())
                .set(DirectionalConfig::getStart_time, dto.getStart_time())
                .set(DirectionalConfig::getEnd_time, dto.getEnd_time())
                .set(DirectionalConfig::getRemark, dto.getRemark()));
        if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

    @DeleteMapping("delete/{id}")
    @AdminPrivilege(and = Privilege.定向配置)
    public Result delete(@PathVariable("id") Long id){
        if(!directionalConfigService.removeById(id)) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

    @GetMapping("list/{uid}")
    @AdminPrivilege(and = Privilege.定向配置)
    public Result list(@PathVariable("uid") Long uid, String admin_username, DirectionalConfigStatus status,
                       String startTime, String endTime,
                       @RequestParam(name = "page", defaultValue = "1") Integer page,
                       @RequestParam(name = "size", defaultValue = "10") Integer size){

//        StatVO statVO = adminBusinessService.getStat(uid);
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        StatVO statVO = StatVO.builder()
                .uid(uid)
                .username(userInfo.getUsername())
                .build();

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<DirectionalConfig> wrapper = new LambdaQueryWrapper<DirectionalConfig>()
                .eq(DirectionalConfig::getUid, uid)
                .like(StringUtils.isNotBlank(admin_username), DirectionalConfig::getAdmin_username, admin_username)
                .ge(StringUtils.isNotBlank(startTime), DirectionalConfig::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), DirectionalConfig::getCreate_time, endTime)
                .orderByDesc(DirectionalConfig::getId);
        if(Objects.nonNull(status)){
            if(DirectionalConfigStatus.effective.equals(status)){
                wrapper.le(DirectionalConfig::getStart_time, now.toString()).ge(DirectionalConfig::getEnd_time, now.toString());
            } else if(DirectionalConfigStatus.ineffective.equals(status)){
                wrapper.gt(DirectionalConfig::getStart_time, now.toString());
            } else if(DirectionalConfigStatus.expired.equals(status)){
                wrapper.lt(DirectionalConfig::getEnd_time, now.toString());
            }
        }
        Page<DirectionalConfig> dto = directionalConfigService.page(new Page<DirectionalConfig>(page, size), wrapper);
        long count = dto.getTotal();
        List<DirectionalConfig> records = dto.getRecords();
        List<DirectionalConfigVO> vo = records.stream().map(e -> DirectionalConfigVO.trans(e, now)).collect(Collectors.toList());
        return Result.success(MapTool.Map().put("count",count).put("list",vo).put("stat",statVO));
    }
}
