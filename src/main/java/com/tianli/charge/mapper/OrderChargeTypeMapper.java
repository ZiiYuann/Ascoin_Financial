package com.tianli.charge.mapper;

import com.tianli.account.vo.OrderChargeTypeVO;
import com.tianli.charge.entity.OrderChargeType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author yangkang
 * @since 2023-03-10
 */
@Mapper
public interface OrderChargeTypeMapper extends BaseMapper<OrderChargeType> {

    @Select("SELECT `type` FROM order_charge_type WHERE operation_group = #{chargeTypeGroup} ")
    List<ChargeType> chargeTypes(@Param("chargeTypeGroup") ChargeTypeGroupEnum chargeTypeGroup);

    @Select("SELECT `type` FROM order_charge_type WHERE operation_type = #{operationType} ")
    List<ChargeType> chargeTypes(@Param("operationType") OperationTypeEnum operationType);

    @Select("SELECT `operation_type` FROM order_charge_type WHERE operation_group = #{chargeTypeGroup} ")
    List<OperationTypeEnum> operationTypes(@Param("chargeTypeGroup") ChargeTypeGroupEnum chargeTypeGroup);
}

