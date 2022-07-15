package com.tianli.charge.converter;

import com.tianli.charge.entity.Order;
import com.tianli.charge.vo.OrderVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeConverter {

    OrderVO toVO(Order order);
}
