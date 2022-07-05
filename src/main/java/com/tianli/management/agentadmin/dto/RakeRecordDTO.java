package com.tianli.management.agentadmin.dto;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-20 16:59
 * @since 1.0.0
 */
@Data
public class RakeRecordDTO {
    private Long id;
    private Long uid;
    private String username;
    private String nick;
    private Long bet_id;
    private BigInteger amount;
    private BigInteger rake;
    private LocalDateTime create_time;
}
