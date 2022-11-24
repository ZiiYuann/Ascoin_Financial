package com.tianli.management.query;

import com.tianli.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialProductIncomeQuery {

    private String productName;

    private String uid;

    private ProductType productType;

    private String coin;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
