package com.tianli.rebate.mapper;

import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.CurrencyTokenEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 返佣表
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Rebate {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 下注id
     */
    private Long bet_id;

    /**
     * 下注类型
     */
    private BetTypeEnum bet_type;

    /**
     * 下注金额
     */
    private BigInteger amount;

    /**
     * 下注用户id
     */
    private Long uid;

    /**
     * 下注用户名
     */
    private String uid_username;

    /**
     * 下注用户昵称
     */
    private String uid_nick;

    /**
     * 下注用户头像
     */
    private String uid_avatar;

    /**
     * 返佣金额
     */
    private BigInteger rebate_amount;
    private CurrencyTokenEnum token;

    /**
     * 返佣用户id
     */
    private Long rebate_uid;

}
