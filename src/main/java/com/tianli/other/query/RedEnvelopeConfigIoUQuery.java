package com.tianli.other.query;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.common.query.IoUQuery;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RedEnvelopeConfigIoUQuery extends IoUQuery {

    private String coin;

    /**
     * 渠道
     */
    private RedEnvelopeChannel channel;

    /**
     * 个数
     */
    private Integer num;

    /**
     * 金额限制
     */
    private BigDecimal limitAmount;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

}
