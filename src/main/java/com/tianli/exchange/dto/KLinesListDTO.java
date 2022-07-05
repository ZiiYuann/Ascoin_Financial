package com.tianli.exchange.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author lzy
 * @date 2022/6/16 10:17
 */
@Data
public class KLinesListDTO {

    @NotBlank
    private String symbol;

    @NotBlank
    private String interval;

    private Long startTime;

    private Long endTime;

    private Integer limit = 500;
}
