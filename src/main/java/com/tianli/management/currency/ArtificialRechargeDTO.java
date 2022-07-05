package com.tianli.management.currency;

import com.tianli.currency.mapper.ArtificialRechargeType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>
 * 人工充值表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class ArtificialRechargeDTO {

    @NotNull(message = "充值用户id不能为空")
    private Long uid;

    @NotNull(message = "类型不能为空")
    private ArtificialRechargeType type;

    @NotNull(message = "充值金额不能为空")
    private Double amount;

    /**
     * 凭证图片["https//www.baidu.com/123.png"]
     */
    @NotNull(message = "充值凭证不能为空")
    @Size(min = 1, message = "充值凭证不能为空")
    private String voucher_image;

    private String remark;
}
