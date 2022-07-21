package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.entity.Order;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialChargeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;


/**
 * @author  chenb
 * @since 2022
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page,
                                         @Param("query") FinancialOrdersQuery financialOrdersQuery);


    IPage<OrderSettleRecordVO> selectOrderSettleInfoVOPage(@Param("page") IPage<OrderSettleRecordVO> page,
                                                           @Param("uid") Long uid,
                                                           @Param("productType") ProductType productType);

    IPage<OrderChargeInfoVO> selectOrderChargeInfoVOPage(@Param("page") IPage<OrderChargeInfoVO> page,
                                                         @Param("query") FinancialChargeQuery query);

    BigDecimal orderChargeSummaryAmount(@Param("query") FinancialChargeQuery query);
}
