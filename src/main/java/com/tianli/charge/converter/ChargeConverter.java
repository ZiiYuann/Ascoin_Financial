package com.tianli.charge.converter;

import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderSettleInfo;
import com.tianli.charge.vo.*;
import com.tianli.financial.entity.FinancialRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeConverter {

    OrderChargeInfoVO toVO(Order order);

    OrderSettleRecordVO toVO(OrderSettleInfo orderSettleInfo);

    OrderRechargeDetailsVo toOrderRechargeDetailsVo(FinancialRecord financialRecord);

    OrderRedeemDetailsVO toOrderRedeemDetailsVO(FinancialRecord financialRecord);

    OrderBaseVO toOrderBaseVO(FinancialRecord financialRecord);
}