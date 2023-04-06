package com.tianli.account.vo;

import com.tianli.charge.entity.OrderChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author:yangkang
 * @create: 2023-03-10 16:41
 * @Description: OrderChargeType VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderChargeTypeVO {

    private ChargeTypeGroupEnum chargeTypeGroup;

    private String group;

    private String groupEn;

    private List<OrderChargeType> orderChargeTypes;

    private Integer order;


    public String getGroupEn() {
        return chargeTypeGroup.getTypeGroupEn();
    }

    public String getGroup() {
        return chargeTypeGroup.getTypeGroup();
    }

    public Integer getOrder() {
        return chargeTypeGroup.getOrder();
    }
}
