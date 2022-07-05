package com.tianli.management.spot.dao;

import com.tianli.management.spot.vo.SGTransactionRecordListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lzy
 * @date 2022/4/16 11:10 上午
 */
@Mapper
public interface SGTransactionRecordMapper {

    Long selectCount(@Param("username") String username,
                     @Param("token") String token,
                     @Param("startTime") String startTime,
                     @Param("endTime") String endTime);

    List<SGTransactionRecordListVo> selectList(@Param("username") String username,
                                               @Param("token") String token,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime,
                                               @Param("page") Integer page,
                                               @Param("size") Integer size);
}
