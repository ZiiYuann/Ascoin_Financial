package com.tianli.product.afund.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;

import com.tianli.product.afund.enums.FundReviewStatus;
import com.tianli.product.afund.enums.FundReviewType;
import lombok.*;

/**
 * <p>
 * 
 * </p>
 *
 * @author xianeng
 * @since 2022-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundReview extends Model<FundReview> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    private Long id;

    /**
     * 类型
     */
    private FundReviewType type;

    /**
     * 关联ID
     */
    private Long rId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     */
    private FundReviewStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
