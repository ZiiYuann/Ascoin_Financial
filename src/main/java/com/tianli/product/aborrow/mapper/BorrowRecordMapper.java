package com.tianli.product.aborrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.aborrow.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Update(" UPDATE `borrow_record` SET `finish` = true ,`finish_time`=#{finishTime},`pledge_status`= 'FINISH' " +
            "WHERE `id`=#{id} AND `uid` =#{uid} AND `finish`= false ")
    int finish(@Param("id") Long id, @Param("uid") Long uid, @Param("finishTime") LocalDateTime finishTime);
}
