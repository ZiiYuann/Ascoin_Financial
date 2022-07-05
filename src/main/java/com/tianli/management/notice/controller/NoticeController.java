package com.tianli.management.notice.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.notice.entity.CreateNoticeDTO;
import com.tianli.management.notice.entity.Notice;
import com.tianli.management.notice.entity.NoticeDTO;
import com.tianli.management.notice.entity.UpdateNoticeDTO;
import com.tianli.management.notice.service.INoticeService;
import com.tianli.management.tutorial.mapper.Tutorial;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private INoticeService iNoticeService;

//  分页查询公告表
    @GetMapping(path = "/page", produces = {"application/json;charset=UTF-8"})
    public Result page(NoticeDTO notice) {
        return iNoticeService.pageAll(notice);
    }

//  新增公告表
    @PostMapping(path = "/list", produces = {"application/json;charset=UTF-8"})
    public Result insert(@RequestBody @Valid CreateNoticeDTO notice) {
        return iNoticeService.insert(notice);
    }

//  更新公告表
    @PostMapping(path = "/update", produces = {"application/json;charset=UTF-8"})
    public Result updateByEntity(@RequestBody @Valid UpdateNoticeDTO dto) {
        boolean update = iNoticeService.update(new LambdaUpdateWrapper<Notice>()
                .eq(Notice::getId, dto.getId())
                .set(Notice::getText, dto.getText())
                .set(Notice::getEn_title, dto.getEn_title())
                .set(Notice::getEn_text, dto.getEn_text())
                .set(Notice::getTh_title, dto.getTh_title())
                .set(Notice::getTh_text, dto.getTh_text())
                .set(Notice::getStatus, dto.getStatus())
                .set(Notice::getTitle, dto.getTitle()));
        if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

//  由id删除公告表
    @DeleteMapping(path = "/list", produces = {"application/json;charset=UTF-8"})
    public Result deleteById(@RequestParam("id") Long id) {
        return iNoticeService.deleteById(id);
    }

//  由id获得公告表
    @GetMapping(path = "/getListById", produces = {"application/json;charset=UTF-8"})
    public Result getListById(@RequestParam("id") Long id) {
        return iNoticeService.getListById(id);
    }
    
    
}
