package com.tianli.accountred.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class RedEnvelopeConfig {

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

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    public static RedEnvelopeConfig defaultConfig() {
        return RedEnvelopeConfig.builder()
                .num(5000)
                .limitAmount(BigDecimal.valueOf(100L))
                .minAmount(BigDecimal.valueOf(0.000001))
                .build();
    }

    public static RedEnvelopeConfig externDefaultConfig() {
        return RedEnvelopeConfig.builder()
                .num(1000)
                .limitAmount(BigDecimal.valueOf(100L))
                .minAmount(BigDecimal.valueOf(0.000001))
                .build();
    }

    public int getScale() {
        String minAmountStr = this.getMinAmount().stripTrailingZeros().toPlainString();
        int scale = 0;
        int i = minAmountStr.indexOf(".");
        if (i != -1) {
            scale = minAmountStr.length() - (i + 1);
        }
        return scale;
    }
}
