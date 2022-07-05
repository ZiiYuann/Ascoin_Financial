package com.tianli.financial.controller;

import lombok.Data;

import javax.validation.constraints.DecimalMin;

@Data
public class PurchaseDTO {
    @DecimalMin(value = "0.0001", message = "购买金额不能为空")
    private double amount;
    private Long product_id;
}
