package com.tianli.management.notice.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
/**
 * <p>
 * 公告表 Mapper 接口
 * </p>
 *
 * @author cc
 * @since 2022-06-09
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

}
