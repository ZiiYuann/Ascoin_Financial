package com.tianli.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimeReq {

    private DateReq date;

    private TimeReq time;
}
