package com.tianli.accountred.query;

import com.tianli.accountred.entity.RedEnvelope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 抢红包query
 *
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGetQuery {

    @NotNull(message = "红包id不允许为空")
    private Long rid;

    @NotBlank(message = "红包唯一标示符号不允许为空")
    private String flag;

    @NotBlank(message = "设备号不允许为空")
    private String deviceNumber;

    private RedEnvelope redEnvelope;

}
