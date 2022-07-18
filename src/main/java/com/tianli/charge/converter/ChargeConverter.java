package com.tianli.charge.converter;

import com.tianli.charge.entity.Order;
import com.tianli.charge.vo.OrderChargeInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeConverter {

    OrderChargeInfoVO toVO(Order order);
}
