package com.tianli.management.query;

import com.tianli.product.afinancial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialProductIncomeQuery {

    private String productName;

    private String uid;

    private List<Long> uids;

    private ProductType productType;

    private String coin;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;



}
