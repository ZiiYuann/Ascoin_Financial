package com.tianli.product.fund.query;

import com.tianli.product.fund.enums.FundTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransactionQuery {

    private Long uid;

    private Long fundId;

    private String queryUid;

    private String queryFundId;

    private String queryProductId;

    private FundTransactionType type;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long agentId;

}
