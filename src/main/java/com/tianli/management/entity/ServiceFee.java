package com.tianli.management.entity;

import com.tianli.common.blockchain.NetworkType;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceFee {

    @Id
    private Long id;

    private LocalDate createTime;

    private String coin;

    private BigDecimal amount;

    private byte type;

    private NetworkType network;
}
