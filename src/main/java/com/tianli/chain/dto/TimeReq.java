package com.tianli.chain.dto;

import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-22
 **/
@Data
public class TimeReq {
    private int hour;
    private int minute;
    private int second;
    private int nano;
}
