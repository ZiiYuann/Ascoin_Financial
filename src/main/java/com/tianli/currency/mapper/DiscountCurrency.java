package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.CurrencyTokenEnum;
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
public class DiscountCurrency extends Model<DiscountCurrency> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 总余额
     */
    private BigInteger balance;
    private CurrencyTokenEnum token;
    private Boolean new_gift;

    private Boolean kyc_certification;
}
