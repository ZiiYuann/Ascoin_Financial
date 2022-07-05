package com.tianli.management.platformfinance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.agent.AgentService;
import com.tianli.bet.BetService;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.chain.mapper.ChainTx;
import com.tianli.chain.service.ChainTxService;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.deposit.ChargeDepositService;
import com.tianli.deposit.mapper.ChargeDeposit;
import com.tianli.deposit.mapper.ChargeDepositStatus;
import com.tianli.deposit.mapper.ChargeDepositType;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import com.tianli.dividends.settlement.mapper.ChargeSettlementType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author chensong
 * @date 2020-12-30 16:34
 * @since 1.0.0
 */
@Service
public class PlatformFinanceService {

    @Resource
    private BetService betService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private ChargeSettlementService chargeSettlementService;

    @Resource
    private ChargeDepositService chargeDepositService;

    @Resource
    private AgentService agentService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private RebateService rebateService;

    @Resource
    private ConfigService configService;

    @Resource
    private ChainTxService chainTxService;

    public Map<String, Object> dividendsDetails(String phone, BetResultEnum result, String startTime, String endTime, Integer page, Integer size) {
        CompletableFuture<Map<String, Object>> summaryCalculation = CompletableFuture.supplyAsync(() -> {
            Map<String, Object> map = MapTool.Map();
            BigInteger totalAmount = betService.selectSumAmountByPlatform(startTime, endTime, result,phone);
            BigInteger totalProfit = betService.selectSumPlatformProfit(startTime, endTime, result,phone);
            BigInteger totalDividends = betService.selectSumAgentDividends(startTime, endTime, result,phone);
            map.put("totalAmount", TokenCurrencyType.usdt_omni.money(totalAmount));
            map.put("totalPlatformDividendsProfit", TokenCurrencyType.usdt_omni.money(totalProfit.subtract(totalDividends)));
            return map;
        });
        LambdaQueryWrapper<Bet> queryWrapper = new LambdaQueryWrapper<Bet>()
                .orderByDesc(Bet::getCreate_time)
                .like(StringUtils.isNotBlank(phone),Bet::getUid_username,phone)
                .eq(Objects.nonNull(result), Bet::getResult, result)
                .le(StringUtils.isNotBlank(endTime), Bet::getCreate_time, endTime)
                .ge(StringUtils.isNotBlank(startTime), Bet::getCreate_time, startTime);
        Page<Bet> pages = betService.page(new Page<>(page, size), queryWrapper);
        long total = pages.getTotal();
        List<DividendDetailsVO> vos = pages.getRecords().stream().map(DividendDetailsVO::trans).collect(Collectors.toList());
        Map<String, Object> map = null;
        try {
            map = summaryCalculation.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return MapTool.Map()
                .put("page", vos)
                .put("totalNum",total)
                .put("totalAmount",map.get("totalAmount"))
                .put("totalPlatformDividendsProfit",map.get("totalPlatformDividendsProfit"));
    }

    private Map<String,BigInteger> getTotalFee(String startTime, String endTime){
        //用户余额提现手续费
        BigInteger userFeeTotalErc20 = ((BigDecimal) chargeService.getMap(
                new QueryWrapper<Charge>().select("ifnull(SUM(`fee`), 0) AS total")
                        .eq("currency_type", TokenCurrencyType.usdt_erc20)
                        .eq("charge_type", ChargeType.withdraw)
                        .eq("status", ChargeStatus.chain_success)
                        .ge(StringUtils.isNotBlank(startTime),"create_time",startTime)
                        .le(StringUtils.isNotBlank(endTime),"create_time",endTime)
        ).get("total")).toBigInteger();
        BigInteger userFeeTotalOmni = ((BigDecimal) chargeService.getMap(
                new QueryWrapper<Charge>().select("ifnull(SUM(`fee`), 0) AS total")
                        .eq("currency_type", TokenCurrencyType.usdt_omni)
                        .eq("charge_type", ChargeType.withdraw)
                        .eq("status", ChargeStatus.chain_success)
                        .ge(StringUtils.isNotBlank(startTime),"create_time",startTime)
                        .le(StringUtils.isNotBlank(endTime),"create_time",endTime)
        ).get("total")).toBigInteger();
        BigInteger userFeeTotal = userFeeTotalOmni.add(userFeeTotalErc20.multiply(new BigInteger("100")));

        //代理商分红结算手续费
        BigInteger settlementFeeTotalErc20 = ((BigDecimal) chargeSettlementService.getMap(
                new QueryWrapper<ChargeSettlement>().select("ifnull(SUM(`fee`), 0) AS total")
                        .eq("currency_type", TokenCurrencyType.usdt_erc20)
                        .eq("charge_type", ChargeSettlementType.withdraw)
                        .eq("status", ChargeSettlementStatus.transaction_success)
                        .ge(StringUtils.isNotBlank(startTime),"create_time",startTime)
                        .le(StringUtils.isNotBlank(endTime),"create_time",endTime)
        ).get("total")).toBigInteger();
        BigInteger settlementFeeTotalOmni = ((BigDecimal) chargeSettlementService.getMap(
                new QueryWrapper<ChargeSettlement>().select("ifnull(SUM(`fee`), 0) AS total")
                        .eq("currency_type", TokenCurrencyType.usdt_omni)
                        .eq("charge_type", ChargeSettlementType.withdraw)
                        .eq("status", ChargeSettlementStatus.transaction_success)
                        .ge(StringUtils.isNotBlank(startTime),"create_time",startTime)
                        .le(StringUtils.isNotBlank(endTime),"create_time",endTime)
        ).get("total")).toBigInteger();
        BigInteger settlementFeeTotal = settlementFeeTotalOmni.add(settlementFeeTotalErc20.multiply(new BigInteger("100")));

        //代理商撤回保证金手续费
        BigInteger depositFeeTotalErc20 = ((BigDecimal) chargeDepositService.getMap(
                new QueryWrapper<ChargeDeposit>().select("ifnull(SUM(`fee`), 0) AS total")
                        .eq("currency_type", TokenCurrencyType.usdt_erc20)
                        .eq("charge_type", ChargeDepositType.withdraw)
                        .eq("status", ChargeDepositStatus.transaction_success)
                        .ge(StringUtils.isNotBlank(startTime),"create_time",startTime)
                        .le(StringUtils.isNotBlank(endTime),"create_time",endTime)
        ).get("total")).toBigInteger();
        BigInteger depositFeeTotalOmni = ((BigDecimal) chargeDepositService.getMap(
                new QueryWrapper<ChargeDeposit>().select("ifnull(SUM(`fee`), 0) AS total")
                        .eq("currency_type", TokenCurrencyType.usdt_omni)
                        .eq("charge_type", ChargeDepositType.withdraw)
                        .eq("status", ChargeDepositStatus.transaction_success)
                        .ge(StringUtils.isNotBlank(startTime),"create_time",startTime)
                        .le(StringUtils.isNotBlank(endTime),"create_time",endTime)
        ).get("total")).toBigInteger();
        BigInteger depositFeeTotal = depositFeeTotalOmni.add(depositFeeTotalErc20.multiply(new BigInteger("100")));
        BigInteger feeTotal = userFeeTotal.add(settlementFeeTotal).add(depositFeeTotal);
        Map<String, BigInteger> map = new HashMap<>();
        map.put("userFeeTotal",userFeeTotal);
        map.put("settlementFeeTotal",settlementFeeTotal);
        map.put("depositFeeTotal",depositFeeTotal);
        map.put("feeTotal",feeTotal);
        return map;
    }

    public Map<String, Object> feeExhibition(Integer page, Integer size) {


        LocalDateTime now = LocalDateTime.now();
        String statistics_start_time = configService.get("statistics_start_time");
        if (StringUtils.isBlank(statistics_start_time)) {
            statistics_start_time = TimeTool.theLast15DaysStr().get("pastDays") + " 00:00:00";
        }

        //数额概览
        Map<String, LocalDate> localDateMap = TimeTool.theLast15Days();
        List<FeeDTO> overviewTemp = chargeService.getDailySumFee(localDateMap.get("pastDays"),localDateMap.get("today"));
        List<FeeOverviewVO> overview = overviewTemp.stream().map(FeeOverviewVO::trans).sorted(Comparator.comparing(FeeOverviewVO::getDate)).collect(Collectors.toList());

        //计算当前页的开始和结束时间
        LocalDate end = now.toLocalDate();
        LocalDate start = LocalDate.parse(statistics_start_time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long totalNum = end.toEpochDay()-start.toEpochDay() + 1;
        int offsetEnd = (page - 1) * size;
        long offsetStart = (page*size > totalNum ? totalNum : page*size) - 1;
        LocalDate startDay = end.minusDays(offsetStart);
        LocalDate endDay = end.minusDays(offsetEnd);
        List<FeeDTO> dailySumFee = chargeService.getDailySumFee(startDay, endDay);
        List<FeeVO> feeVOList = dailySumFee.stream().map(FeeVO::trans).sorted(Comparator.comparing(FeeVO::getCreateTime).reversed()).collect(Collectors.toList());

        return MapTool.Map()
                .put("overview",overview)
                .put("page",feeVOList).put("totalNum",totalNum);
    }

    public Map<String, Object> feeExhibition1(Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();
        String statistics_start_time = configService.get("statistics_start_time");
        if (StringUtils.isBlank(statistics_start_time)) {
            statistics_start_time = TimeTool.theLast15DaysStr().get("pastDays") + " 00:00:00";
        }

        //总数额
//        Map<String, BigInteger> totalFeeMap = getTotalFee(statistics_start_time, now.toString());
        String finalStatistics_start_time = statistics_start_time;
        CompletableFuture<Map<String, BigInteger>> future = CompletableFuture.supplyAsync(() -> getTotalFee(finalStatistics_start_time, now.toString()));

        //数额概览
        Map<String, LocalDate> localDateMap = TimeTool.theLast15Days();
        List<FeeDTO> overviewTemp = chargeService.getDailySumFee(localDateMap.get("pastDays"),localDateMap.get("today"));
        List<FeeOverviewVO> overview = overviewTemp.stream().map(FeeOverviewVO::trans).sorted(Comparator.comparing(FeeOverviewVO::getDate)).collect(Collectors.toList());

        //计算当前页的开始和结束时间
        LocalDate end = now.toLocalDate();
        LocalDate start = LocalDate.parse(statistics_start_time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long totalNum = end.toEpochDay()-start.toEpochDay() + 1;
        int offsetEnd = (page - 1) * size;
        long offsetStart = (page*size > totalNum ? totalNum : page*size) - 1;
        LocalDate startDay = end.minusDays(offsetStart);
        LocalDate endDay = end.minusDays(offsetEnd);
        //每日明细
        List<FinanceExhibitionDetailDTO> dailyFeeDetails = chargeService.getDailyFeeDetails(startDay, endDay);
        List<FinanceExhibitionDetailVO> feeVOList = dailyFeeDetails.stream().map(FinanceExhibitionDetailVO::trans).sorted(Comparator.comparing(FinanceExhibitionDetailVO::getCreateTime).reversed()).collect(Collectors.toList());

        Map<String, BigInteger> totalFeeMap = null;
        try {
            totalFeeMap = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //总手续费
        BigInteger feeTotal = totalFeeMap.get("feeTotal");
        //用户余额提现手续费（USDT)
        BigInteger userFeeTotal = totalFeeMap.get("userFeeTotal");
        //代理商分红结算手续费
        BigInteger settlementFeeTotal = totalFeeMap.get("settlementFeeTotal");
        //代理商撤回保证金手续费
        BigInteger depositFeeTotal = totalFeeMap.get("depositFeeTotal");

        return MapTool.Map()
                .put("feeTotal",Objects.nonNull(feeTotal) ? TokenCurrencyType.usdt_omni.money(feeTotal) : 0.0)
                .put("userFeeTotal",Objects.nonNull(userFeeTotal) ? TokenCurrencyType.usdt_omni.money(userFeeTotal) : 0.0)
                .put("settlementFeeTotal",Objects.nonNull(settlementFeeTotal) ? TokenCurrencyType.usdt_omni.money(settlementFeeTotal) : 0.0)
                .put("depositFeeTotal",Objects.nonNull(depositFeeTotal) ? TokenCurrencyType.usdt_omni.money(depositFeeTotal) : 0.0)
                .put("overview", overview)
                .put("page",feeVOList)
                .put("totalNums",totalNum);
    }

    public Map<String, Object> financeExhibition(String startTime, String endTime, Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();

        String statistics_start_time = configService.get("statistics_start_time");
        if (StringUtils.isBlank(statistics_start_time)) {
            statistics_start_time = TimeTool.theLast15DaysStr().get("pastDays") + " 00:00:00";
        }
        LocalDateTime firstDay = LocalDateTime.parse(statistics_start_time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //实际要查询的日期
        LocalDateTime queryStartTime = firstDay;
        LocalDateTime queryEndTime = now;
        if(StringUtils.isNotBlank(startTime) &&
                firstDay.compareTo(
                        LocalDateTime.parse(startTime ,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) < 0){
            queryStartTime = LocalDateTime.parse(startTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if(StringUtils.isNotBlank(endTime) &&
                now.compareTo(
                        LocalDateTime. parse(endTime ,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) > 0){
            queryEndTime = LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        Duration between = Duration.between(TimeTool.minDay(queryStartTime),queryEndTime);
        int totalNum = (int)between.toDays() + 1;

        //总手续费
        Map<String, BigInteger> totalFeeMap = getTotalFee(startTime, endTime);
        BigInteger feeTotal = totalFeeMap.get("feeTotal");

        //平台分红净盈亏数额
        BigInteger totalProfit = betService.selectSumPlatformProfit(queryStartTime.toString(), queryEndTime.toString(),null,null);
        BigInteger totalDividends = betService.selectSumAgentDividends(queryStartTime.toString(), queryEndTime.toString(),null ,null);
        BigInteger dividendsProfitTotal = totalProfit.subtract(totalDividends);

        //总反佣
        BigInteger rebateTotal = ((BigDecimal) rebateService.getMap(new QueryWrapper<Rebate>()
                .select("ifnull(SUM(`rebate_amount`), 0) AS total")
                .between("create_time", queryStartTime, queryEndTime))
                .get("total")).toBigInteger();

        //代理商净盈亏
        BigInteger agentProfitTotal = totalDividends;
        //代理商分红已结算数额
//        BigInteger agentSettledTotal = ((BigDecimal) agentService.getMap(new QueryWrapper<Agent>()
//                .select("ifnull(SUM(`settled_number`), 0) AS total")
//                .eq("`identity`","senior_agent")
//                .between("create_time", queryStartTime, queryEndTime))
//                .get("total")).toBigInteger();
        //代理商未结算数额
//        BigInteger agentUnsettlementTotal = agentProfitTotal.subtract(agentSettledTotal);
        //代理商分红已结算数额
        BigInteger agentSettledTotal = ((BigDecimal) currencyLogService.getMap(new QueryWrapper<CurrencyLog>()
                .select("ifnull(SUM(CASE WHEN log_type = 'increase' or log_type = 'increase' THEN -`amount` ELSE `amount` END ), 0) AS total")
                .eq("`type`","settlement")
                .eq("`des`","结算")
                .in("`log_type`", Lists.newArrayList(CurrencyLogType.increase, CurrencyLogType.reduce, CurrencyLogType.withdraw))
                .between("create_time", queryStartTime, queryEndTime))
                .get("total")).toBigInteger();
        BigInteger agentUnsettlementTotal = agentProfitTotal.subtract(agentSettledTotal);


        int offsetEnd = (page - 1) * size;
        int offsetStart = (page*size > totalNum ? totalNum : page*size)-1;
        LocalDateTime end = queryEndTime.minusDays(offsetEnd);
        LocalDateTime timeTemp = TimeTool.minDay(queryEndTime.minusDays(offsetStart));
        LocalDateTime start = timeTemp.compareTo(queryStartTime) < 0 ? queryStartTime:timeTemp;
        List<FinanceExhibitionDTO> dto = betService.getDailyAmount(start, end);
        List<FinanceExhibitionVO> voList = dto.stream().map(FinanceExhibitionVO::trans).collect(Collectors.toList());

        return MapTool.Map()
                .put("userFeeTotal",TokenCurrencyType.usdt_omni.money(feeTotal))
                .put("dividendsProfitTotal",TokenCurrencyType.usdt_omni.money(dividendsProfitTotal))
                .put("rebateTotal",TokenCurrencyType.usdt_omni.money(rebateTotal))
                .put("agentProfitTotal",TokenCurrencyType.usdt_omni.money(agentProfitTotal))
                .put("agentSettledTotal",TokenCurrencyType.usdt_omni.money(agentSettledTotal))
                .put("agentUnsettlementTotal",TokenCurrencyType.usdt_omni.money(agentUnsettlementTotal))
                .put("page",voList).put("totalNums",totalNum);
    }


    public Map<String, Object> financeExhibition2(String startTime, String endTime, Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();

        String statistics_start_time = configService.get("statistics_start_time");
        if (StringUtils.isBlank(statistics_start_time)) {
            statistics_start_time = TimeTool.theLast15DaysStr().get("pastDays") + " 00:00:00";
        }
        LocalDateTime firstDay = LocalDateTime.parse(statistics_start_time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //实际要查询的日期
        LocalDateTime queryStartTime = firstDay;
        LocalDateTime queryEndTime = now;
        if(StringUtils.isNotBlank(startTime) &&
                firstDay.compareTo(
                        LocalDateTime.parse(startTime ,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) < 0){
            queryStartTime = LocalDateTime.parse(startTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if(StringUtils.isNotBlank(endTime) &&
                now.compareTo(
                        LocalDateTime. parse(endTime ,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) > 0){
            queryEndTime = LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        Duration between = Duration.between(TimeTool.minDay(queryStartTime),queryEndTime);
        int totalNum = (int)between.toDays() + 1;

        LocalDateTime finalQueryStartTime = queryStartTime;
        LocalDateTime finalQueryEndTime = queryEndTime;
        CompletableFuture<Map<String,Object>> supplyAsync = CompletableFuture.supplyAsync(() -> {
            //平台押注净盈亏&平台抽水
            Map<String, Object> betMap = betService.getMap(new QueryWrapper<Bet>()
                    .select("ifnull(SUM(`platform_profit`), 0) AS sum_platform_profit",
                            "ifnull(SUM(`fee`), 0) AS sum_fee")
                    .ge( "create_time", finalQueryStartTime)
                    .le("create_time", finalQueryEndTime));
            BigInteger sumPlatformProfit = ((BigDecimal) betMap.get("sum_platform_profit")).toBigInteger();
            BigInteger sumRake = ((BigDecimal) betMap.get("sum_fee")).toBigInteger();


            //手续费
            Map<String, Object> chargeMap =  chargeService.getMap(
                    new QueryWrapper<Charge>().select(
                            "ifnull(SUM(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END), 0) AS erc20_fee",
                            "ifnull(SUM(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END), 0) AS omni_fee",
                            "ifnull(SUM(CASE WHEN `miner_fee_type` = 'eth' THEN `miner_fee` ELSE 0 END), 0) AS miner_eth_fee",
                            "ifnull(SUM(CASE WHEN `miner_fee_type` = 'btc' THEN `miner_fee` ELSE 0 END), 0) AS miner_btc_fee")
                            .eq("charge_type", ChargeType.withdraw)
                            .eq("status", ChargeStatus.chain_success)
                            .ge("create_time",finalQueryStartTime)
                            .le("create_time",finalQueryEndTime)
            );
            BigInteger userFeeTotalOmni = ((BigDecimal) chargeMap.get("omni_fee")).toBigInteger();
            BigInteger userFeeTotalErc20 = ((BigDecimal) chargeMap.get("erc20_fee")).toBigInteger();
            BigInteger userFeeTotal = userFeeTotalOmni.add(userFeeTotalErc20.multiply(new BigInteger("100")));

            //反佣数额
            Map<String, Object> rebateMap = rebateService.getMap(new QueryWrapper<Rebate>()
                    .select("ifnull(SUM(`rebate_amount`), 0) AS sum_amount")
                    .ge( "create_time", finalQueryStartTime)
                    .le( "create_time", finalQueryEndTime));
            BigInteger rebate = ((BigDecimal) rebateMap.get("sum_amount")).toBigInteger();

            //代理商抽水数额&利息
            Map<String, Object> currencyLogMap = currencyLogService.getMap(new QueryWrapper<CurrencyLog>()
                    .select("ifnull(SUM(CASE WHEN `des` = '利息' THEN `amount` ELSE 0 END ), 0) AS interest",
                            "ifnull(SUM(CASE WHEN `des` = '抽水' THEN `amount` ELSE 0 END ), 0) AS rake")
                    .ge( "create_time", finalQueryStartTime)
                    .le( "create_time", finalQueryEndTime));
            BigInteger interest = ((BigDecimal) currencyLogMap.get("interest")).toBigInteger();
            BigInteger agentRake = ((BigDecimal) currencyLogMap.get("rake")).toBigInteger();

            //归集成本
            Map<String, Object> chainMap = chainTxService.getMap(new QueryWrapper<ChainTx>()
                    .select("ifnull(SUM(CASE WHEN `fee_currency_type` = 'eth' THEN `fee` ELSE 0 END), 0) AS miner_eth_fee, " +
                            "ifnull(SUM(CASE WHEN `fee_currency_type` = 'btc' THEN `fee` ELSE 0 END), 0) AS miner_btc_fee")
                    .eq("`status`", "chain_success")
                    .ge( "complete_time", finalQueryStartTime)
                    .le("complete_time", finalQueryEndTime));
            BigInteger miner_eth_fee = ((BigDecimal) chainMap.get("miner_eth_fee")).toBigInteger();
            BigInteger miner_btc_fee = ((BigDecimal) chainMap.get("miner_btc_fee")).toBigInteger();

            //撤回矿工费
            BigInteger charge_miner_eth_fee = ((BigDecimal) chargeMap.get("miner_eth_fee")).toBigInteger();
            BigInteger charge_miner_btc_fee = ((BigDecimal) chargeMap.get("miner_btc_fee")).toBigInteger();

            //总矿工费
            BigInteger miner_eth_fee_total = miner_eth_fee.add(charge_miner_eth_fee);
            BigInteger miner_btc_fee_total = miner_btc_fee.add(charge_miner_btc_fee);

            //矿工费汇率换算
            DigitalCurrency miner_btc_fee_to_omni = new DigitalCurrency(TokenCurrencyType.btc, miner_btc_fee_total).toOther(TokenCurrencyType.usdt_omni);
            DigitalCurrency miner_eth_fee_to_omni = new DigitalCurrency(TokenCurrencyType.eth, miner_eth_fee_total).toOther(TokenCurrencyType.usdt_omni);

            BigInteger miner_fee_total = miner_btc_fee_to_omni.getAmount().add(miner_eth_fee_to_omni.getAmount());

            BigInteger grossProfit = sumPlatformProfit.add(userFeeTotal).add(sumRake).subtract(rebate).subtract(agentRake).subtract(interest).subtract(miner_fee_total);
            return MapTool.Map()
                    .put("sum_platform_profit",TokenCurrencyType.usdt_omni.money(sumPlatformProfit))
                    .put("sum_fee",TokenCurrencyType.usdt_omni.money(userFeeTotal))
                    .put("sum_platform_rake",TokenCurrencyType.usdt_omni.money(sumRake))
                    .put("sum_rebate",TokenCurrencyType.usdt_omni.money(rebate))
                    .put("sum_agent_rake",TokenCurrencyType.usdt_omni.money(agentRake))
                    .put("sum_interest",TokenCurrencyType.usdt_omni.money(interest))
                    .put("miner_fee",TokenCurrencyType.usdt_omni.money(miner_fee_total))
                    .put("miner_btc_fee",TokenCurrencyType.btc.money(miner_btc_fee_total))
                    .put("miner_eth_fee",TokenCurrencyType.eth.money(miner_eth_fee_total))
                    .put("gross_profit",TokenCurrencyType.usdt_omni.money(grossProfit));
        });

        int offsetEnd = (page - 1) * size;
        int offsetStart = (page*size > totalNum ? totalNum : page*size)-1;
        LocalDateTime end = queryEndTime.minusDays(offsetEnd);
        LocalDateTime timeTemp = TimeTool.minDay(queryEndTime.minusDays(offsetStart));
        LocalDateTime start = timeTemp.compareTo(queryStartTime) < 0 ? queryStartTime:timeTemp;

        List<FinanceDailyBreakdownDTO> dto = betService.getDailyBreakdown(start,end);
        List<FinanceDailyBreakdownVO> vos = dto.stream().map(FinanceDailyBreakdownVO::trans).collect(Collectors.toList());
        Map<String, Object> statMap = null;
        try {
            statMap = supplyAsync.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return MapTool.Map().put("stat",statMap).put("totalNums",totalNum).put("page",vos);
    }


    /**
     * 平台财务展板
     */
    public Map<String, Object> financeExhibition3(String startTime, String endTime, Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();

        String statistics_start_time = configService.get("statistics_start_time");
        if (StringUtils.isBlank(statistics_start_time)) {
            statistics_start_time = TimeTool.theLast15DaysStr().get("pastDays") + " 00:00:00";
        }
        LocalDateTime firstDay = LocalDateTime.parse(statistics_start_time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //实际要查询的日期
        LocalDateTime queryStartTime = firstDay;
        LocalDateTime queryEndTime = now;
        if(StringUtils.isNotBlank(startTime) &&
                firstDay.compareTo(
                        LocalDateTime.parse(startTime ,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) < 0){
            queryStartTime = LocalDateTime.parse(startTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if(StringUtils.isNotBlank(endTime) &&
                now.compareTo(
                        LocalDateTime. parse(endTime ,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) > 0){
            queryEndTime = LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        Duration between = Duration.between(TimeTool.minDay(queryStartTime),queryEndTime);
        int totalNum = (int)between.toDays() + 1;

        LocalDateTime finalQueryStartTime = queryStartTime;
        LocalDateTime finalQueryEndTime = queryEndTime;
        //数额概要
        CompletableFuture<Map<String,Object>> supplyAsync = CompletableFuture.supplyAsync(() -> {
            //平台分红净盈亏数额
            BigInteger totalProfit = betService.selectSumPlatformProfit(finalQueryStartTime.toString(), finalQueryEndTime.toString(),null,null);
            BigInteger totalDividends = betService.selectSumAgentDividends(finalQueryStartTime.toString(), finalQueryEndTime.toString(),null ,null);
            BigInteger dividendsProfitTotal = totalProfit.subtract(totalDividends);

            //反佣数额
            Map<String, Object> rebateMap = rebateService.getMap(new QueryWrapper<Rebate>()
                    .select("ifnull(SUM(`rebate_amount`), 0) AS sum_amount")
                    .ge( "create_time", finalQueryStartTime)
                    .le( "create_time", finalQueryEndTime));
            BigInteger rebate = ((BigDecimal) rebateMap.get("sum_amount")).toBigInteger();

            //代理商净盈亏
            BigInteger agentProfitTotal = totalDividends;

            //代理商分红已结算数额
            BigInteger agentSettledTotal = ((BigDecimal) currencyLogService.getMap(new QueryWrapper<CurrencyLog>()
                    .select("ifnull(SUM(CASE WHEN log_type = 'increase' or log_type = 'increase' THEN -`amount` ELSE `amount` END ), 0) AS total")
                    .eq("`type`","settlement")
                    .eq("`des`","结算")
                    .in("`log_type`", Lists.newArrayList(CurrencyLogType.increase, CurrencyLogType.reduce, CurrencyLogType.withdraw))
                    .between("create_time", finalQueryStartTime, finalQueryEndTime))
                    .get("total")).toBigInteger();

            //代理商分红未结算数额
            BigInteger agentUnsettlementTotal = agentProfitTotal.subtract(agentSettledTotal);

            //手续费
            Map<String, Object> chargeMap =  chargeService.getMap(
                    new QueryWrapper<Charge>().select(
                            "ifnull(SUM(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END), 0) AS erc20_fee",
                            "ifnull(SUM(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END), 0) AS omni_fee",
                            "ifnull(SUM(CASE WHEN `miner_fee_type` = 'eth' THEN `miner_fee` ELSE 0 END), 0) AS miner_eth_fee",
                            "ifnull(SUM(CASE WHEN `miner_fee_type` = 'btc' THEN `miner_fee` ELSE 0 END), 0) AS miner_btc_fee")
                            .eq("charge_type", ChargeType.withdraw)
                            .eq("status", ChargeStatus.chain_success)
                            .ge("create_time",finalQueryStartTime)
                            .le("create_time",finalQueryEndTime)
            );
            BigInteger userFeeTotalOmni = ((BigDecimal) chargeMap.get("omni_fee")).toBigInteger();
            BigInteger userFeeTotalErc20 = ((BigDecimal) chargeMap.get("erc20_fee")).toBigInteger();
            BigInteger userFeeTotal = userFeeTotalOmni.add(userFeeTotalErc20.multiply(new BigInteger("100")));

            //归集成本
            Map<String, Object> chainMap = chainTxService.getMap(new QueryWrapper<ChainTx>()
                    .select("ifnull(SUM(CASE WHEN `fee_currency_type` = 'eth' THEN `fee` ELSE 0 END), 0) AS miner_eth_fee, " +
                            "ifnull(SUM(CASE WHEN `fee_currency_type` = 'btc' THEN `fee` ELSE 0 END), 0) AS miner_btc_fee")
                    .eq("`status`", "chain_success")
                    .ge( "complete_time", finalQueryStartTime)
                    .le("complete_time", finalQueryEndTime));
            BigInteger miner_eth_fee = ((BigDecimal) chainMap.get("miner_eth_fee")).toBigInteger();
            BigInteger miner_btc_fee = ((BigDecimal) chainMap.get("miner_btc_fee")).toBigInteger();

            //撤回矿工费
            BigInteger charge_miner_eth_fee = ((BigDecimal) chargeMap.get("miner_eth_fee")).toBigInteger();
            BigInteger charge_miner_btc_fee = ((BigDecimal) chargeMap.get("miner_btc_fee")).toBigInteger();

            //总矿工费
            BigInteger miner_eth_fee_total = miner_eth_fee.add(charge_miner_eth_fee);
            BigInteger miner_btc_fee_total = miner_btc_fee.add(charge_miner_btc_fee);

            //矿工费汇率换算
            DigitalCurrency miner_btc_fee_to_omni = new DigitalCurrency(TokenCurrencyType.btc, miner_btc_fee_total).toOther(TokenCurrencyType.usdt_omni);
            DigitalCurrency miner_eth_fee_to_omni = new DigitalCurrency(TokenCurrencyType.eth, miner_eth_fee_total).toOther(TokenCurrencyType.usdt_omni);

            BigInteger miner_fee_total = miner_btc_fee_to_omni.getAmount().add(miner_eth_fee_to_omni.getAmount());

            //毛利 平台分红净盈亏+手续费-返佣数额-矿工费
            BigInteger grossProfit = dividendsProfitTotal.add(userFeeTotal).subtract(rebate).subtract(miner_fee_total);
            return MapTool.Map()
                    .put("dividendsProfitTotal",TokenCurrencyType.usdt_omni.money(dividendsProfitTotal)) //平台分红净盈亏
                    .put("sum_fee",TokenCurrencyType.usdt_omni.money(userFeeTotal)) //总手续费数额
                    .put("agentProfitTotal",TokenCurrencyType.usdt_omni.money(agentProfitTotal)) //代理商分红净盈亏数额
                    .put("sum_rebate",TokenCurrencyType.usdt_omni.money(rebate)) //总返佣数额
                    .put("agentSettledTotal",TokenCurrencyType.usdt_omni.money(agentSettledTotal)) //代理商分红已结算数额
                    .put("agentUnsettlementTotal",TokenCurrencyType.usdt_omni.money(agentUnsettlementTotal)) //代理商分红当前未结算数额
                    .put("miner_fee",TokenCurrencyType.usdt_omni.money(miner_fee_total)) //归集成本
                    .put("miner_btc_fee",TokenCurrencyType.btc.money(miner_btc_fee_total))
                    .put("miner_eth_fee",TokenCurrencyType.eth.money(miner_eth_fee_total))
                    .put("gross_profit",TokenCurrencyType.usdt_omni.money(grossProfit)); //毛利
        });

        int offsetEnd = (page - 1) * size;
        int offsetStart = (page*size > totalNum ? totalNum : page*size)-1;
        LocalDateTime end = queryEndTime.minusDays(offsetEnd);
        LocalDateTime timeTemp = TimeTool.minDay(queryEndTime.minusDays(offsetStart));
        LocalDateTime start = timeTemp.compareTo(queryStartTime) < 0 ? queryStartTime:timeTemp;

        List<FinanceDailyBreakdownDetailsDTO> dto = betService.getDailyBreakdownDetails(start,end);
        List<FinanceDailyBreakdownDetailsVO> vos = dto.stream().map(FinanceDailyBreakdownDetailsVO::trans).collect(Collectors.toList());
        Map<String, Object> statMap = null;
        try {
            statMap = supplyAsync.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return MapTool.Map().put("stat",statMap).put("totalNums",totalNum).put("page",vos);
    }

}
