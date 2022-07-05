package com.tianli.management.user.controller;

import lombok.Data;

@Data
public class CustomerUpdateCreditScoreDTO {
    private Integer credit_score;
    private String adjust_reason;
}
