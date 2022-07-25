package com.tianli.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-22
 **/
@Data
@AllArgsConstructor
public class TimeReq {
    private int hour;
    private int minute;
    private int second;
    private int nano;
}
