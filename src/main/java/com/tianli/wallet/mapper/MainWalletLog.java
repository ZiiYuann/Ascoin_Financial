package com.tianli.wallet.mapper;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.common.blockchain.CurrencyNetworkType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 主钱包交易记录
 * </p>
 */

@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MainWalletLog {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 时间
     */
    private LocalDateTime createTime;

    /**
     * 货币类型
     */
    private String currencyType;

    /**
     * 链的类型, bsc, trc, erc
     */
    private CurrencyNetworkType chainType;

    /**
     * 金额
     */
    private BigInteger amount;

    /**
     * 转出地址
     */
    private String fromAddress;

    /**
     * 转入地址
     */
    private String toAddress;

    /**
     * 转入/转出方向 in/out
     */
    private String direction;

    /**
     * 交易hash
     */
    private String txid;

    /**
     * 交易区块
     */
    private String block;

}
