package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.management.query.FinancialRechargeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
@Mapper
public interface OrderChargeInfoMapper extends BaseMapper<OrderChargeInfo> {
    
}