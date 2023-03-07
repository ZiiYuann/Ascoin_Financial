package com.tianli.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReceiptLogDTO {

    private String fromAddress;

    private String toAddress;

    private BigDecimal amount;

}
