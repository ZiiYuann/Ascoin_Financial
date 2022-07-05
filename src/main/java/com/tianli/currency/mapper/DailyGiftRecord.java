package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户每日奖励记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class DailyGiftRecord extends Model<DailyGiftRecord> {

    private static final long serialVersionUID = -2313298059641553429L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 用户账户
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nick;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 领取日期
     */
    private LocalDate receive_date;

    /**
     * 余额类型
     */
    private TokenCurrencyType token;

    /**
     * 数额
     */
    private BigInteger amount;
}
