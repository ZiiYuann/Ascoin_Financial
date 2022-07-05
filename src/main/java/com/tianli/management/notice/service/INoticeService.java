package com.tianli.management.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.exception.Result;
import com.tianli.management.notice.entity.CreateNoticeDTO;
import com.tianli.management.notice.entity.Notice;
import com.tianli.management.notice.entity.NoticeDTO;
import com.tianli.management.notice.entity.UpdateNoticeDTO;

/**
 * <p>
 * 公告表 服务类
 * </p>
 *
 * @author cc
 * @since 2022-06-09
 */
public interface INoticeService extends IService<Notice> {

    Result pageAll(NoticeDTO notice);

    Result select(Notice notice);

    Result insert(CreateNoticeDTO notice);

    Result deleteById(Long id);

    Result getListById(Long id);
}
