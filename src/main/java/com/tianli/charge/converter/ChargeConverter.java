package com.tianli.charge.converter;

import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderReview;
import com.tianli.charge.vo.*;
import com.tianli.product.afinancial.entity.FinancialRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeConverter {

    OrderChargeInfoVO toVO(Order order);

    OrderRechargeDetailsVo toOrderRechargeDetailsVo(FinancialRecord financialRecord);

    OrderRedeemDetailsVO toOrderRedeemDetailsVO(FinancialRecord financialRecord);

    OrderBaseVO toOrderBaseVO(FinancialRecord financialRecord);

    OrderReviewVO toOrderReviewVO(OrderReview orderReview);

    OrderVO toOrderVO(Order order);
}
