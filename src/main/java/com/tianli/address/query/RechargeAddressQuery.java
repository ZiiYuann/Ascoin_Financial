package com.tianli.address.query;

import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote 充值参数
 * @since 2022-07-07
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeAddressQuery {

    /**
     * 币种信息
     */
    private TokenCurrencyType type;

    /**
     * 交易hash
     */
    private String txId;

    /**
     * 扣款地址
     */
    private String fromAddress;

    /**
     * 收款地址
     */
    private String toAddress;

    /**
     * 区块
     */
    private String block;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private String sn;

    /**
     * 金额
     */
    private BigInteger value;

}
