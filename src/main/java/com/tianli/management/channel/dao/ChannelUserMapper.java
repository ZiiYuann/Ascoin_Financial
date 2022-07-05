package com.tianli.management.channel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.channel.entity.ChannelUser;
import com.tianli.management.channel.vo.ChannelCpaListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lzy
 * @date 2022/5/7 11:38
 */
@Mapper
public interface ChannelUserMapper extends BaseMapper<ChannelUser> {

    Long cpaCount(@Param("channelIds") List<Long> channelIds,
                  @Param("username") String username,
                  @Param("kycStatus") Integer kycStatus,
                  @Param("startTime") String startTime,
                  @Param("endTime") String endTime);

    List<ChannelCpaListVo> cpaList(@Param("channelIds") List<Long> channelIds,
                                   @Param("username") String username,
                                   @Param("kycStatus") Integer kycStatus,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime,
                                   @Param("page") Integer page,
                                   @Param("size") Integer size);
}
