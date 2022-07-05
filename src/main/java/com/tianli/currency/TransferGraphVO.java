package com.tianli.currency;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TransferGraphVO {
    private String id;
    private String from;
    private String to;
    private String coinAddress;
    private BigInteger block;
    private BigInteger value;
    private BigInteger transferTime;
}
