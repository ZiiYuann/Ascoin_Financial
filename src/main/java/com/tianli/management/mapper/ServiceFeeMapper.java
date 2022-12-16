package com.tianli.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.entity.ServiceFee;
import com.tianli.management.query.TimeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Mapper
public interface ServiceFeeMapper extends BaseMapper<ServiceFee> {


    /**
     * 获取时间段内段手续费
     *
     * @param query time query
     * @param type  类型
     * @return 手续费集合
     */
    List<ServiceFee> getTotalAmount(@Param("query") TimeQuery query, @Param("type") byte type);
}
