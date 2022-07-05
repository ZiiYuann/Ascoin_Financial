package com.tianli.operate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.operate.OperateService;
import com.tianli.operate.mapper.OperateConf;
import com.tianli.operate.operateType.OperateTypeService;
import com.tianli.operate.operateType.mapper.OperateType;
import com.tianli.operate.otc.SaveOperateDTO;
import com.tianli.operate.otc.SaveOperateTransDTO;
import com.tianli.operate.otc.UpdateOperateDTO;
import com.tianli.operate.otc.UpdateOperateTransDTO;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

/**
 * 运营位配置表 前端控制器
 *
 * @author linyifan
 *  2/24/21 3:01 PM
 */

@RestController
@RequestMapping("/operate")
public class OperateController {

    @GetMapping("/banner/list")
    public Result list(@RequestParam(value = "location", defaultValue = "1")Integer location,
                       @RequestParam(value = "type",defaultValue = "1")Integer type){
        return Result.success(operateService.operateClientPage(location, type));
    }

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.运营位配置)
    public Result page(@RequestParam(value = "location",defaultValue = "1")Integer location,
                       @RequestParam(value = "type",defaultValue = "1")Integer type,
                       @RequestParam(value = "page",defaultValue = "1")Integer page,
                       @RequestParam(value = "size",defaultValue = "10")Integer size){
        return Result.success(operateService.operatePage(location, type, page, size));
    }

    @GetMapping("/location/list")
    @AdminPrivilege(and = Privilege.运营位配置)
    public Result locationList(){
        String operateConfig = configService.get("operate_config_list");
        if (StringUtils.isBlank(operateConfig)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        OperateConf operateConf = new Gson().fromJson(operateConfig, OperateConf.class);
        return Result.success(MapTool.Map().put("locationList",operateConf));
    }

    @PostMapping("/add")
    @AdminPrivilege(and = Privilege.运营位配置)
    public Result addOperate(@RequestBody @Valid SaveOperateDTO saveOperateDTO){
        //参数校验
        if((Objects.isNull(saveOperateDTO.getValidity()) || !saveOperateDTO.getValidity()) && (Objects.isNull(saveOperateDTO.getStartTime()) || Objects.isNull(saveOperateDTO.getEndTime()))){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        if(Objects.isNull(saveOperateDTO.getLocation()) && CollectionUtils.isEmpty(saveOperateDTO.getLocations())){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        SaveOperateTransDTO trans = SaveOperateTransDTO.trans(saveOperateDTO);
        operateService.saveOperate(trans);
        return Result.success();
    }

    @PostMapping("/update")
    @AdminPrivilege(and = Privilege.运营位配置)
    public Result updateOperate(@RequestBody @Valid UpdateOperateDTO operateDTO){
        //参数校验
        if(Objects.isNull(operateDTO.getValidity()) && (Objects.isNull(operateDTO.getStartTime()) || Objects.isNull(operateDTO.getEndTime()))){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        if(Objects.isNull(operateDTO.getLocation()) && CollectionUtils.isEmpty(operateDTO.getLocations())){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        UpdateOperateTransDTO trans = UpdateOperateTransDTO.trans(operateDTO);
        operateService.updateOperate(trans);
        return Result.success();
    }

    @Transactional
    @DeleteMapping("/del/{operateId}")
    @AdminPrivilege(and = Privilege.运营位配置)
    public Result deleteOperate(@PathVariable("operateId")Long operateId){
        boolean removeById = operateService.removeById(operateId);
        if(!removeById) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        boolean remove = operateTypeService.remove(new LambdaQueryWrapper<OperateType>().eq(OperateType::getOperate_id, operateId));
        if (!remove) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return Result.success();
    }

    @Resource
    private OperateService operateService;

    @Resource
    private OperateTypeService operateTypeService;

    @Resource
    private ConfigService configService;
}
