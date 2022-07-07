package com.tianli.currency.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * <p>
 * 用户余额表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class Currency extends Model<Currency> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 余额类型
     */
    private TokenCurrencyType type;

    /**
     * 总余额
     */
    private BigInteger balance;

    /**
     * 冻结余额
     */
    private BigInteger freeze;

    /**
     * 剩余余额
     */
    private BigInteger remain;

    /**
     * 总余额
     */
    private BigInteger balanceBF;

    /**
     * 冻结余额
     */
    private BigInteger freezeBF;

    /**
     * 剩余余额
     */
    private BigInteger remainBF;
}
