package com.tianli.management.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.exception.Result;
import com.tianli.management.notice.dao.NoticeMapper;
import com.tianli.management.notice.entity.CreateNoticeDTO;
import com.tianli.management.notice.entity.Notice;
import com.tianli.management.notice.entity.NoticeDTO;
import com.tianli.management.notice.entity.UpdateNoticeDTO;
import com.tianli.management.notice.service.INoticeService;
import com.tianli.management.tutorial.mapper.Tutorial;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 公告表 服务实现类
 * </p>
 *
 * @author cc
 * @since 2022-06-09
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public Result pageAll(NoticeDTO notice) {
        Page<Notice> page = noticeMapper.selectPage(new Page<>(notice.getPage(), notice.getSize()),
                new LambdaQueryWrapper<Notice>()
                        .orderByDesc(Notice::getId)
                        .and(StringUtils.isNotBlank(notice.getTitle()), e -> e.like(Notice::getTitle, notice.getTitle()).or()
                                .like(Notice::getTh_title, notice.getTitle()).or()
                                .like(Notice::getEn_title, notice.getTitle()))
                        .ge(StringUtils.isNotBlank(notice.getStartTime()), Notice::getCreate_time, notice.getStartTime())
                        .le(StringUtils.isNotBlank(notice.getEndTime()), Notice::getCreate_time, notice.getEndTime())
                        .eq(Objects.nonNull(notice.getStatus()), Notice::getStatus, notice.getStatus())
        );
        return Result.success(page);
    }

    @Override
    public Result select(Notice notice) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        List<Notice> list = noticeMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    @Override
    public Result insert(CreateNoticeDTO notice) {
        noticeMapper.insert(Notice.builder()
                .id(CommonFunction.generalId())
                .title(notice.getTitle()).text(notice.getText())
                .en_title(notice.getEn_title()).en_text(notice.getEn_text())
                .th_title(notice.getTh_title()).th_text(notice.getTh_text())
                .status(notice.getStatus())
                .create_time(LocalDateTime.now()).build());
        return Result.success("新增成功");
    }

    @Override
    public Result deleteById(Long id) {
        noticeMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result getListById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        return Result.success(notice);
    }

}
