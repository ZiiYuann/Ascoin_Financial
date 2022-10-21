package com.tianli.accountred.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeChainQuery {

    @NotNull
    private Long id;

    @NotBlank(message = "交易hash不允许为空")
    private String txid;
}