package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.entity.Order;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * @author  chenb
 * @since 2022
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select(" select * from order where order_no = #{orderNo}")
    Order getBySn(@Param("orderNo") String orderNo);

    IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page,
                                         @Param("query") FinancialOrdersQuery financialOrdersQuery);
}
