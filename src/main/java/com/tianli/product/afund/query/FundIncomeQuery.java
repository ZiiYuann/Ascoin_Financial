package com.tianli.product.afund.query;

import com.tianli.product.afund.enums.FundRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundIncomeQuery {

    private Long uid;

    private Long fundId;

    private String queryUid;

    private String queryProductId;

    // 1-已计算 2-待审核 3-已发放 4-审核失败
    private List<Integer> status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private FundRecordStatus recordStatus;

    private Long productId;

    private Long agentId;
}
