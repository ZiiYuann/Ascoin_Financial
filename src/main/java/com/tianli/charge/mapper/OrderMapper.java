package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.ServiceAmountQuery;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialChargeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

    BigDecimal orderAmountSum(@Param("query") FinancialChargeQuery query);

    @Select("SELECT SUM(amount) FROM `order` WHERE type=#{chargeType} AND complete_time BETWEEN #{startTime} and #{endTime} ")
    BigDecimal amountSumByCompleteTime(@Param("chargeType") ChargeType chargeType,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    BigDecimal serviceAmountSumByCompleteTime(@Param("query") ServiceAmountQuery query);
}
