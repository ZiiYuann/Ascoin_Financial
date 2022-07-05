package com.tianli.financial.controller;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class UserFinancialPage {
    private Long id;
    private String name;
    private String name_en;
    private BigInteger amount;
    private double money;
    private double profit;
//    private long period;
    private LocalDate start_date;
    private LocalDate end_date;
    private String type;
    private String status;
    private double rate;
    private String logo;
    private Long period;
}
