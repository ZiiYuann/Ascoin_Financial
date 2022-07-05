package com.tianli.bet.controller;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

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
public class BetPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 创建时间 ms
     */
    private Long create_time_ms;

    /**
     * 完成时间
     */
    private LocalDateTime complete_time;

    /**
     * 完成时间 ms
     */
    private Long complete_time_ms;

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
    private double amount;
    private double real_amount;

    /**
     * 优惠金额
     */
    private double discount_amount;

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
     * 最终方向
     */
    private KlineDirectionEnum final_direction;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;

    /**
     * 盈利所得
     */
    private double earn;

    /**
     * 平台手续费
     */
    private double fee;

    /**
     * 净利润
     */
    private double profit;

    /**
     * 最终所得
     */
    private double income;
    /**
     * 奖励的BF
     */
    private double income_BF;

    private double final_BF;

    /**
     * 押注币种交易对
     */
    private String bet_symbol;
    private String bet_symbol_name;

    public static BetPageVO trans(Bet bet) {
        BetPageVO betPageVO = new BetPageVO();
        BeanUtils.copyProperties(bet, betPageVO);
        betPageVO.setId(bet.getId().toString());
        Instant create_instant = bet.getCreate_time().atZone(ZoneId.systemDefault()).toInstant();
        betPageVO.setCreate_time_ms(create_instant.toEpochMilli());
        if(Objects.nonNull(bet.getComplete_time())){
            Instant complete_instant = bet.getComplete_time().atZone(ZoneId.systemDefault()).toInstant();
            betPageVO.setComplete_time_ms(complete_instant.toEpochMilli());
        }
        betPageVO.setAmount(TokenCurrencyType.usdt_omni.money(bet.getAmount()));
        betPageVO.setReal_amount(TokenCurrencyType.usdt_omni.money(bet.getReal_amount()));
        betPageVO.setDiscount_amount(TokenCurrencyType.usdt_omni.money(bet.getDiscount_amount()));
        BigInteger earn = bet.getEarn();
        betPageVO.setEarn(TokenCurrencyType.usdt_omni.money(earn == null ? BigInteger.ZERO : earn));
        BigInteger fee = bet.getFee();
        betPageVO.setFee(TokenCurrencyType.usdt_omni.money(fee == null ? BigInteger.ZERO : fee));
        BigInteger profit = bet.getProfit();
        betPageVO.setProfit(TokenCurrencyType.usdt_omni.money(profit == null ? BigInteger.ZERO : profit));
        BigInteger income = bet.getIncome();
        betPageVO.setIncome(TokenCurrencyType.usdt_omni.money(income == null ? BigInteger.ZERO : income));
        BigInteger final_bf = bet.getFinal_BF();
        betPageVO.setFinal_BF(TokenCurrencyType.BF_bep20.money(final_bf == null ? BigInteger.ZERO : final_bf));
        BigInteger income_bf = bet.getIncome_BF();
        betPageVO.setIncome_BF(TokenCurrencyType.BF_bep20.money(income_bf == null ? BigInteger.ZERO : income_bf));
        return betPageVO;
    }

}
