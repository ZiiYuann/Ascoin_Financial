package com.tianli.agent.management.vo;

import com.tianli.product.afund.enums.FundReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundReviewVO {

    private Long id;

    /**
     * 状态
     */
    private FundReviewStatus status;

    /**
     * 关联ID
     */
    private Long rId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
