package com.tianli.account.vo;

import com.tianli.charge.entity.OrderChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author:yangkang
 * @create: 2023-03-10 16:41
 * @Description: OrderChargeType VO
 */
@NoArgsConstructor
@AllArgsConstructor
public class OrderChargeTypeVO {
    private String group;

    private String operationGroupName;

    private List<OrderChargeType> orderChargeTypes;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<OrderChargeType> getOrderChargeTypes() {
        return orderChargeTypes;
    }

    public void setOrderChargeTypes(List<OrderChargeType> orderChargeTypes) {
        this.orderChargeTypes = orderChargeTypes;
    }

    public String getOperationGroupName() {
        return operationGroupName;
    }

    public void setOperationGroupName(String operationGroupName) {
        this.operationGroupName = operationGroupName;
    }
}