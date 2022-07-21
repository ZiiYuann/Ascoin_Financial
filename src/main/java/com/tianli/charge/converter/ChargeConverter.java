package com.tianli.charge.converter;

import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderSettleInfo;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeConverter {

    OrderChargeInfoVO toVO(Order order);

    OrderSettleInfoVO toVO(OrderSettleInfo orderSettleInfo);
}
