package com.tianli.currency.controller;

import com.tianli.currency.log.CurrencyLogDes;
import lombok.Data;

@Data
public class LogPageDTO {
    private int page = 1;
    private int size = 10;
    private CurrencyLogDes des;
}
