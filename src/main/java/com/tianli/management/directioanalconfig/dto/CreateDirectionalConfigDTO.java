package com.tianli.management.directioanalconfig.dto;

import com.tianli.bet.mapper.BetResultEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author chensong
 *  2021-03-04 11:19
 * @since 1.0.0
 */
@Data
public class CreateDirectionalConfigDTO {

    @NotNull(message = "uid不能为空")
    private Long uid;
    private String currency_type;
    @NotNull(message = "请输入走向")
    private BetResultEnum result;
    @NotNull(message = "请输入开始时间")
    private LocalDateTime start_time;
    @NotNull(message = "请输入结束时间")
    private LocalDateTime end_time;
    private String remark;
}
