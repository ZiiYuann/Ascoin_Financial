package com.tianli.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.entity.ServiceFee;
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


    @Select("SELECT sum(`amount`) as amount,coin,network  FROM `service_fee` where type = #{type} group by coin")
    List<ServiceFee> getTotalAmount(@Param("type") byte type);
}
