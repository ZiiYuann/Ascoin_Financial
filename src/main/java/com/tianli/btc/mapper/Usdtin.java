package com.tianli.btc.mapper;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author cs
 * @Date 2022-01-18 9:43 上午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("usdt_in")
public class Usdtin {
    @TableId
    private String txid;
    private String sendingaddress;
    private String referenceaddress;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long amount;
    private Long block;
    private LocalDateTime create_time;
    private Long fee;
    private Boolean valid;
}
