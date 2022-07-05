package com.tianli.management.ruleconfig.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BetDuration {

    private Integer id;

    private Double duration;

    private BigInteger min_bet_amount;

    private BigInteger max_bet_amount;

    private Double extra_percentage;

}
