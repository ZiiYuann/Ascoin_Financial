package com.tianli.account.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 用户余额汇总表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AccountBalance extends Model<AccountBalance> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * logo 地址
     */
    private String logo;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 余额类型
     */
    private CurrencyCoin coin;

    /**
     * 总余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal freeze;

    /**
     * 剩余余额
     */
    private BigDecimal remain;

}
