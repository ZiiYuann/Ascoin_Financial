package com.tianli.accountred.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-04
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeSpiltDTO {

    /**
     * 拆分红包id
     */
    private String id;

    /**
     * 红包id
     */
    private String rid;

    /**
     * 红包金额
     */
    private BigDecimal amount;

    /**
     * 是否领取
     */
    private boolean receive;

}
