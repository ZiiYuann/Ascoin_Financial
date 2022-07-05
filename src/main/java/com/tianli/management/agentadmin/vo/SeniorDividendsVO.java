package com.tianli.management.agentadmin.vo;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.mapper.Dividends;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Objects;

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
public class SeniorDividendsVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 下注id
     */
    private Long bet_id;

    /**
     * 下注用户名
     */
    private String uid_username;

    /**
     * 下注用户昵称
     */
    private String uid_nick;

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
    private double amount;

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
     * 总净盈亏
     */
    private double all_profit;

    /**
     * 本级净盈亏
     */
    private double my_profit;

    private CurrencyTokenEnum profit_token;

    public static SeniorDividendsVO trans(Dividends dividends){
        TokenCurrencyType currencyType = dividends.getProfit_token() == CurrencyTokenEnum.usdt_omni ?
                TokenCurrencyType.usdt_omni : TokenCurrencyType.BF_bep20;
        return SeniorDividendsVO.builder()
                .id(dividends.getId())
                .uid_username(dividends.getUid_username())
                .bet_id(dividends.getBet_id())
                .uid_nick(dividends.getUid_nick())
                .bet_type(dividends.getBet_type())
                .bet_time(dividends.getBet_time())
                .bet_create_time(dividends.getBet_create_time())
                .amount(TokenCurrencyType.usdt_omni.money(dividends.getAmount()))
                .bet_direction(dividends.getBet_direction())
                .final_direction(dividends.getFinal_direction())
                .result(dividends.getResult())
                .all_profit(Objects.isNull(dividends.getAll_profit()) ? 0 : currencyType.money(dividends.getAll_profit()))
                .my_profit(Objects.isNull(dividends.getMy_profit()) ? 0 : currencyType.money(dividends.getMy_profit()))
                .profit_token(dividends.getProfit_token())
                .build();
    }

}
