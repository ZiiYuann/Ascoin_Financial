package com.tianli.accountred.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

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
public class RedEnvelopeExchangeCodeQuery {

    @NotBlank(message = "设备号不允许为空")
    private String deviceNumber;

    @NotBlank(message = "兑换码不允许为空")
    private String exchangeCode;
}
