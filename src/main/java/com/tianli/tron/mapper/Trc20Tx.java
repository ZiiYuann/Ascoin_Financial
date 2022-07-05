package com.tianli.tron.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @Author cs
 * @Date 2022-01-07 2:46 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trc20Tx {
    private long id;
    private String txid;
    private long block;
    private String owner_address;
    private String contract_address;
    private String to_address;
    private BigInteger amount;
    private LocalDateTime create_time;
    private Integer status;

    private BigInteger net_fee;
    private BigInteger energy_fee;
    private long net_usage;
    private long energy_usage;
    private long origin_energy_usage;
    private long energy_usage_total;
}
