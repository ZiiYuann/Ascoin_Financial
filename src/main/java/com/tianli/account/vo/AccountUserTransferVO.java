package com.tianli.account.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountUserTransferVO {

    private Long id;

    private Long transferUid;

    private Long receiveUid;

    private String coin;

    private BigDecimal amount;

    private LocalDateTime createTime;

    private String transferOrderNo;

    private Long externalPk;
}
