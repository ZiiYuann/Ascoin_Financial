package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.entity.Order;
import com.tianli.charge.vo.OrderSettleInfoVO;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * @author  chenb
 * @since 2022
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page,
                                         @Param("query") FinancialOrdersQuery financialOrdersQuery);


    IPage<OrderSettleInfoVO> selectOrderSettleInfoVOPage(@Param("page") IPage<OrderSettleInfoVO> page,
                                          @Param("uid") Long uid,
                                          @Param("productType") ProductType productType);
}
