package com.tianli.bet.mapper;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.currency.CurrencyTokenEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 押注表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Bet {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 完成时间
     */
    private LocalDateTime complete_time;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 用户username
     */
    private String uid_username;

    /**
     * 用户昵称
     */
    private String uid_nick;

    /**
     * 用户头像
     */
    private String uid_avatar;

    /**
     * 押注类型
     */
    private BetTypeEnum bet_type;

    /**
     * 竞猜时间,单位:分钟
     */
    private Double bet_time;

    /**
     * 押注金额
     */
    private BigInteger amount;

    /**
     * 实际计算奖励及其结算的金额
     */
    private BigInteger real_amount;

    /**
     * 押注方向
     */
    private KlineDirectionEnum bet_direction;

    /**
     * 开始费率
     */
    private Double start_exchange_rate;

    /**
     * 结束费率
     */
    private Double end_exchange_rate;

    /**
     * 结束费率终结者
     */
    private Double prophecy_end_exchange_rate;
    private Double slip_difference;

    /**
     * 最终方向
     */
    private KlineDirectionEnum final_direction;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;

    /**
     * 盈利所得
     * 订单结果导致的本来的收益
     * amount / -amount
     */
    private BigInteger earn;

    /**
     * 平台手续费
     * 押注win时, 平台扣除的手续费
     */
    private BigInteger fee;

    /**
     * 净利润
     * earn - fee
     */
    private BigInteger profit;

    /**
     * 最终所得(只会是USDT)
     * 押注win:
     *      income == amount + earn         --> 用了BF抵扣了应该扣除的fee的Usdt
     *      income == amount + earn - fee   --> 没有使用BF抵扣USDT(正常情况下)
     */
    private BigInteger income;

    /**
     * 押注奖励的BF
     */
    private BigInteger income_BF;

    /**
     * 手续费用BF抵扣时, 消耗的BF(原始的)
     */
    private BigInteger base_BF;

    /**
     * 手续费用BF抵扣时, 消耗的BF(打折后)
     */
    private BigInteger final_BF;

    /**
     * 平台净利润(用户净利润负数)
     */
    private BigInteger platform_profit;

    /**
     * 代理商分红
     */
    private BigInteger agent_dividends;

    /**
     * 平台返佣
     */
    private BigInteger platform_rebate;

    /**
     * 押注币种交易对
     */
    private String bet_symbol;

    /**
     * 优惠金额
     */
    private BigInteger discount_amount;
    /**
     * 贷款金额
     */
    private BigInteger loan_amount;

    /**
     * 利润的代币类型
     */
    private CurrencyTokenEnum profit_token;
    /**
     * 利润的代币类型
     */
    private BigInteger pf_profit;
    /**
     * 利润的代币类型
     */
    private BigInteger agent_profit;
    /**
     * 利润的代币类型
     */
    private BigInteger chain_profit;
    /**
     * 利润的代币类型
     */
    private BigInteger surplus_profit;

    /**
     * 上级代理商id
     */
    private Long agent_id;
    /**
     * 上级代理商用户名
     */
    private String agent_username;

    /**
     * 是否是机器人订单
     */
    private String order_type;
}
