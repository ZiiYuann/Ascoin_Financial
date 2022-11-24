package com.tianli.management.query;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.query.IoUQuery;
import com.tianli.management.enums.HotWalletOperationType;
import lombok.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HotWalletDetailedIoUQuery extends IoUQuery {

    /**
     * 如果id存在则是修改
     */
    private Long id;

    private String uid;

    @NotNull(message = "金额填写有误")
    @DecimalMin(value = "0.00000001", message = "金额填写有误")
    private BigDecimal amount;

    @NotNull(message = "币别不允许为空")
    private String coin;

    @NotNull(message = "链不允许为空")
    private ChainType chain;

    @NotBlank(message = "发送地址不允许为空")
    private String fromAddress;

    @NotBlank(message = "接受地址不允许为空")
    private String toAddress;

    @NotBlank(message = "交易hash不允许为空")
    private String hash;

    @NotNull(message = "类型不能为null")
    private HotWalletOperationType type;

    private String remarks;
}
