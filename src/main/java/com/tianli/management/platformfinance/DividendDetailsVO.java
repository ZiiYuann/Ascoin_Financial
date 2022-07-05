package com.tianli.management.platformfinance;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DividendDetailsVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 账户手机号
     */
    private String uid_username;

    /**
     * 账户姓名
     */
    private String uid_nick;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 押注类型
     */
    private BetTypeEnum betType;

    /**
     * 竞猜时间,单位:分钟
     */
    private Double betTime;

    /**
     * 押注金额
     */
    private Double amount;

    /**
     * 押注方向
     */
    private KlineDirectionEnum betDirection;

    /**
     * 最终方向
     */
    private KlineDirectionEnum finalDirection;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;


    /**
     * 净利润
     */
    private Double profit;

    /**
     * 平台分红净盈亏
     */
    private Double platformDividendsProfit;

    public static DividendDetailsVO trans(Bet bet){
        DividendDetailsVO vo = new DividendDetailsVO();
        BigInteger amount = bet.getAmount() == null ? BigInteger.ZERO : bet.getAmount();
        BigInteger profit = bet.getProfit() == null ? BigInteger.ZERO : bet.getProfit();
        BigInteger platformProfit = bet.getPlatform_profit() == null ? BigInteger.ZERO : bet.getPlatform_profit();
        BigInteger agentDividends = bet.getAgent_dividends() == null ? BigInteger.ZERO : bet.getAgent_dividends();
        vo.setId(bet.getId())
                .setUid_username(bet.getUid_username())
                .setUid_nick(bet.getUid_nick())
                .setCreateTime(bet.getCreate_time())
                .setBetType(bet.getBet_type())
                .setBetTime(bet.getBet_time())
                .setAmount(TokenCurrencyType.usdt_omni.money(amount))
                .setBetDirection(bet.getBet_direction())
                .setFinalDirection(bet.getFinal_direction())
                .setResult(bet.getResult())
                .setProfit(TokenCurrencyType.usdt_omni.money(profit))
                .setPlatformDividendsProfit(
                        TokenCurrencyType.usdt_omni.money(platformProfit.subtract(agentDividends)));
        return vo;
    }
}
