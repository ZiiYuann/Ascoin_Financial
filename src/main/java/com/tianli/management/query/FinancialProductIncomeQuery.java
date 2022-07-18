package com.tianli.management.query;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
public class FinancialProductIncomeQuery {

    private String productName;

    private Long uid;

    private ProductType productType;

    private CurrencyCoin coin;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
