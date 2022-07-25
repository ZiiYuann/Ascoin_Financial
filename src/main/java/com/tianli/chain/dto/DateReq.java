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
public class DateReq {
    private int year;
    private int month;
    private int day;
}
