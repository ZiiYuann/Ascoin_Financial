package com.tianli.bet;


import com.tianli.agent.mapper.Agent;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.currency.CurrencyTokenEnum;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Map;

/**
 * <p>
 * 押注分红 服务类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
public interface BetDividendsService {

    /**
     * 获取系统手续费比例
     */
    double getSysRebateRate();

    /**
     * 获取平台抽水比例
     */
    double getPlatformRebateRate();

    /**
     * 计算代理商分红
     */
    void calculationAgentDividends(Bet dbBet, Bet bet);

    /**
     * 押注赢的情况下
     *
     * @param bet                数据库押注数据
     * @param expectedResultTime 预计出结果时间
     * @param result             最终走势 涨跌平
     * @param betResult          押注结果: 输赢
     * @param startExchangeRate  开始费率
     * @param endExchangeRate    结束费率
     * @return 包含id及所需更新字段的bet对象
     */
    Bet winCaseExe(Bet bet, LocalDateTime expectedResultTime, KlineDirectionEnum result, BetResultEnum betResult, double dividendsRate, double startExchangeRate, double endExchangeRate);

    /**
     * 押注输的情况下
     *
     * @param bet                数据库押注数据
     * @param expectedResultTime 预计出结果时间
     * @param result             最终走势 涨跌平
     * @param betResult          押注结果: 输赢
     * @param startExchangeRate  开始费率
     * @param endExchangeRate    结束费率
     * @return 包含id及所需更新字段的bet对象
     */
    Bet loseCaseExe(Bet bet, LocalDateTime expectedResultTime, KlineDirectionEnum result, BetResultEnum betResult, double startExchangeRate, double endExchangeRate);

    static double rate(String str) {
        double result = 0.0;
        if (!StringUtils.isEmpty(str)) {
            result = Double.parseDouble(str);
        }
        if (result < 0.0) result = 0.0;
        if (result > 1.0) result = 1.0;
        return result;
    }

    /**
     * 计算代理商分红 版本2
     * @param bet
     * @param updateBet
     */
    void calculationAgentDividendsV2(Bet bet, Bet updateBet);

    /**
     * 计算代理商分红 版本3
     * @param bet
     * @param updateBet
     */
    void calculationAgentDividendsV3(Bet bet, Bet updateBet);
    Map<String, Object> transBF(Long uid, BigDecimal total_dividends, Bet bet, Bet dbBet);

    void doRebates(LinkedList<Long> list, BigDecimal total_dividends, CurrencyTokenEnum token, Bet bet, Bet dbBet, Long uid);

    BigDecimal doDividends(Agent agent, Bet dbBet, Bet bet, BigDecimal total_dividends, CurrencyTokenEnum token);
}
