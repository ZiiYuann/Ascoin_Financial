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
public class AddressMnemonic {
    private Long id;
    private Long addressId;
    private String mnemonic;
    private LocalDateTime createTime;
}
