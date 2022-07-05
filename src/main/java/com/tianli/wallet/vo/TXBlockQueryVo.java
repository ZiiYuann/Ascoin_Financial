package com.tianli.wallet.vo;

import lombok.Data;

import java.math.BigInteger;

/**
 * @author lzy
 * @date 2022/4/27 16:40
 */
@Data
public class TXBlockQueryVo {

    private Long id;

    private String hash;

    private String from;

    private String to;

    private String contractAddress;

    private Long block;

    private String createTime;

    private BigInteger value;
}
