package com.tianli.management.tutorial.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.tutorial.dto.CreateTutorialDTO;
import com.tianli.management.tutorial.dto.UpdateTutorialDTO;
import com.tianli.management.tutorial.mapper.Tutorial;
import com.tianli.management.tutorial.mapper.TutorialStatus;
import com.tianli.management.tutorial.service.TutorialService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chensong
 * @date 2021-02-24 14:33
 * @since 1.0.0
 */
@RestController
@RequestMapping("tutorial")
public class TutorialController {

    @Resource
    private TutorialService tutorialService;

    @GetMapping("page")
    @AdminPrivilege(and = Privilege.教程管理)
    public Result tutorialPage(String title, TutorialStatus status, String startTime, String endTime,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size){
        Page<Tutorial> tutorialPage = tutorialService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Tutorial>()
                        .orderByDesc(Tutorial::getId)
                        .and(StringUtils.isNotBlank(title), e -> e.like(Tutorial::getTitle, title).or()
                                .like(Tutorial::getEn_title, title))
                        .ge(StringUtils.isNotBlank(startTime), Tutorial::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), Tutorial::getCreate_time, endTime)
                        .eq(Objects.nonNull(status), Tutorial::getStatus, status));
        return Result.success(MapTool.Map().put("count",tutorialPage.getTotal()).put("list",tutorialPage.getRecords()));
    }

    @PostMapping("create")
    @AdminPrivilege(and = Privilege.教程管理)
    public Result createTutorial(@RequestBody @Valid CreateTutorialDTO dto){
        Tutorial tutorial = Tutorial.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .status(dto.getStatus())
                .en_text(dto.getEn_text())
                .text(dto.getText())
                .title(dto.getTitle())
                .en_title(dto.getEn_title())
                .th_text(dto.getTh_text())
                .th_title(dto.getTh_title())
                .build();
        if(!tutorialService.save(tutorial)) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

    @PostMapping("update")
    @AdminPrivilege(and = Privilege.教程管理)
    public Result updateTutorial(@RequestBody @Valid UpdateTutorialDTO dto){
        boolean update = tutorialService.update(new LambdaUpdateWrapper<Tutorial>()
                .eq(Tutorial::getId, dto.getId())
                .set(Tutorial::getText, dto.getText())
                .set(Tutorial::getEn_title, dto.getEn_title())
                .set(Tutorial::getEn_text, dto.getEn_text())
                .set(Tutorial::getTh_title, dto.getTh_title())
                .set(Tutorial::getTh_text, dto.getTh_text())
                .set(Tutorial::getStatus, dto.getStatus())
                .set(Tutorial::getTitle, dto.getTitle()));
        if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

//    @PutMapping("update/{status}/{id}")
    @AdminPrivilege(and = Privilege.教程管理)
    public Result updateStatus(@PathVariable("id") Long id, @PathVariable("status") TutorialStatus status){
        if(!tutorialService.update(new LambdaUpdateWrapper<Tutorial>().set(Tutorial::getStatus,status).eq(Tutorial::getId,id))){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        return Result.success();
    }

    @DeleteMapping("delete/{id}")
    @AdminPrivilege(and = Privilege.教程管理)
    public Result deleteTutorial(@PathVariable("id") Long id){
        if(!tutorialService.removeById(id)) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }
}
