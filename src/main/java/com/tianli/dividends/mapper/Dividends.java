package com.tianli.dividends.mapper;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.BetResultEnum;
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
 * 分红表
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Dividends {

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
     * 竞猜时间,单位:分钟
     */
    private Double bet_time;

    /**
     * 押注时间
     */
    private LocalDateTime bet_create_time;

    /**
     * 押注金额
     */
    private BigInteger amount;

    /**
     * 押注方向
     */
    private KlineDirectionEnum bet_direction;

    /**
     * 最终方向
     */
    private KlineDirectionEnum final_direction;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;

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
     * 分红用户id
     */
    private Long dividends_uid;

    /**
     * 总净盈亏
     */
    private BigInteger all_profit;

    /**
     * 上级净盈亏
     */
    private BigInteger senior_profit;

    /**
     * 本级净盈亏
     */
    private BigInteger my_profit;

    /**
     * 下级净盈亏
     */
    private BigInteger low_profit;

    /**
     * 分红的代币类型
     */
    private CurrencyTokenEnum profit_token;
}
