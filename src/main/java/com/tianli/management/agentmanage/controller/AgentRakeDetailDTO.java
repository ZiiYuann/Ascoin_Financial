package com.tianli.management.agentmanage.controller;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-20 15:51
 * @since 1.0.0
 */
@Data
public class AgentRakeDetailDTO {
    private Long id;
    private String nick;
    private String username;
    private LocalDateTime create_time;
    private BigInteger amount;
}
