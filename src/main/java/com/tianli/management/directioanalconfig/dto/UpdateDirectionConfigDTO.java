package com.tianli.management.directioanalconfig.dto;

import com.tianli.bet.mapper.BetResultEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 *  2021-03-04 11:46
 * @since 1.0.0
 */
@Data
public class UpdateDirectionConfigDTO {
    private Long id;
    private String currency_type;
    private BetResultEnum result;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private String remark;
}
