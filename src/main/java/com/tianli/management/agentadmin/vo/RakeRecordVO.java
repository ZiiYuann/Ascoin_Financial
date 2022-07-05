package com.tianli.management.agentadmin.vo;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.agentadmin.dto.RakeRecordDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-20 17:01
 * @since 1.0.0
 */
@Data
@Builder
public class RakeRecordVO {
    private Long id;
    private String username;
    private String nick;
    private Long bet_id;
    private Double amount;
    private Double rake;
    private LocalDateTime create_time;

    public static RakeRecordVO trans(RakeRecordDTO dto){
        return RakeRecordVO.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .nick(dto.getNick())
                .bet_id(dto.getBet_id())
                .amount(TokenCurrencyType.usdt_omni.money(dto.getAmount()))
                .rake(TokenCurrencyType.usdt_omni.money(dto.getRake()))
                .create_time(dto.getCreate_time()).build();
    }
}
