package com.tianli.other.query;

import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.common.query.IoUQuery;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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

    @NotEmpty(message = "币种不能为空")
    private String coin;

    /**
     * 渠道
     */
    @NotNull(message = "红包类型不能为空")
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
