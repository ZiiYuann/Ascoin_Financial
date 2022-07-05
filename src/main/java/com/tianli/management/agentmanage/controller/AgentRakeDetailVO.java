package com.tianli.management.agentmanage.controller;

import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-20 16:18
 * @since 1.0.0
 */
@Data
@Builder
public class AgentRakeDetailVO {
    private Long id;
    private String nick;
    private String username;
    private LocalDateTime create_time;
    private Double amount;

    public static AgentRakeDetailVO trans(AgentRakeDetailDTO dto){
        return AgentRakeDetailVO.builder()
                .id(dto.getId())
                .nick(dto.getNick())
                .username(dto.getUsername())
                .create_time(dto.getCreate_time())
                .amount(TokenCurrencyType.usdt_omni.money(dto.getAmount())).build();
    }
}
