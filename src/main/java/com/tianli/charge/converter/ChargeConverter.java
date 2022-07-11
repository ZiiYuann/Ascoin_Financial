package com.tianli.charge.converter;

import com.tianli.charge.entity.Charge;
import com.tianli.charge.vo.ChargeVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeConverter {

    ChargeVO toVO(Charge charge);
}
