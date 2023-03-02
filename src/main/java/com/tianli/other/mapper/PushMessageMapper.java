package com.tianli.other.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.other.entity.PushMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-02
 **/
@Mapper
public interface PushMessageMapper extends BaseMapper<PushMessage> {
}
