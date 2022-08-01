package com.tianli.chain.entity;

import com.tianli.chain.enums.ImputationStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletImputation {

    @Id
    private Long id;

    private Long uid;

    private Long addressId;

    private String address;

    private BigDecimal amount;

    private CurrencyCoin coin;

    private NetworkType network;

    private ImputationStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
