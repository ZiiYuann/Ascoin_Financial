package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.charge.entity.OrderAdvance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderAdvanceMapper extends BaseMapper<OrderAdvance> {

    @Update("update order_advance set try_times = try_times + 1 where id =#{id}")
    int addTryTimes(@Param("id") Long id);
}
