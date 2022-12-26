package com.tianli.address.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author cs
 * @Date 2022-12-26 10:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeAddressMnemonic {
    private Long id;
    private Long uid;
    private String mnemonic;
    private LocalDateTime createTime;
}
