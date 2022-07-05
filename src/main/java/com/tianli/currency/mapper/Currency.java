package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.CurrencyTypeEnum;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
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
    private CurrencyTypeEnum type;

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
    private BigInteger balance_BF;

    /**
     * 冻结余额
     */
    private BigInteger freeze_BF;

    /**
     * 剩余余额
     */
    private BigInteger remain_BF;
}
