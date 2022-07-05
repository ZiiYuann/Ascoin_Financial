package com.tianli.currency_token.transfer.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TransferDTO {
    private Long id;
    private String hash;
    private String from;
    private String to;
    private String contractAddress;
    private Long block;
    private String createTime;
    private BigInteger value;
    private String status;
}
