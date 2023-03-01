package com.tianli.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidateDTO {

    @JsonProperty(value = "record_id")
    private Long recordId;

    private String coin;

    @JsonProperty(value = "to_coin")
    private String toCoin;

    private BigDecimal amount;
}
