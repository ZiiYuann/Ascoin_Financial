package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.common.blockchain.CurrencyCoinEnum;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户币变动记录表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyTokenLog extends Model<CurrencyTokenLog> {

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
    private CurrencyCoinEnum token;

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
    private CurrencyLogDes des;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

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
