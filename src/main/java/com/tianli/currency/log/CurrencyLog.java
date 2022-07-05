package com.tianli.currency.log;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.CurrencyTypeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 余额变动记录表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyLog extends Model<CurrencyLog> {

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
     * 币种类型
     */
    private CurrencyTokenEnum token;

    /**
     * 订单号
     */
    private String sn;

    /**
     * 记录类型
     */
    private CurrencyLogType log_type;

    /**
     * 余额变动描述
     */
    private String des;

    /**
     * 金额
     */
    private BigInteger amount;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

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
}
