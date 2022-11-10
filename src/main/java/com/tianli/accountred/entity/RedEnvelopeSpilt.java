package com.tianli.accountred.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拆分红包
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeSpilt {

    /**
     * 拆分红包id
     */
    @Id
    private String id;

    /**
     * 红包id
     */
    private Long rid;

    /**
     * 红包金额
     */
    private BigDecimal amount;

    /**
     * 是否领取
     */
    private boolean receive;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;


}
