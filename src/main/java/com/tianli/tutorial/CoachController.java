package com.tianli.tutorial;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.exception.Result;
import com.tianli.management.tutorial.mapper.Tutorial;
import com.tianli.management.tutorial.mapper.TutorialStatus;
import com.tianli.management.tutorial.service.TutorialService;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chensong
 * @date 2021-02-24 14:33
 * @since 1.0.0
 */
@RestController
@RequestMapping("/coach")
public class CoachController {
    @Resource
    private TutorialService tutorialService;
    @GetMapping("/page")
    public Result tutorialPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size){
        Page<Tutorial> tutorialPage = tutorialService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Tutorial>()
                        .orderByDesc(Tutorial::getId)
                        .eq(Tutorial::getStatus, TutorialStatus.enable));
        return Result.success(MapTool.Map().put("total",tutorialPage.getTotal()).put("list",tutorialPage.getRecords()));
    }


}
