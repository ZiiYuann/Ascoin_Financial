package com.tianli.management.agentadmin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.agent.team.AgentTeamService;
import com.tianli.agent.team.mapper.AgentTeam;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.charge.ChargeService;
import com.tianli.common.CommonFunction;
import com.tianli.common.DoubleDecimalTrans;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.mapper.Currency;
import com.tianli.deposit.ChargeDepositService;
import com.tianli.deposit.LowDepositService;
import com.tianli.deposit.mapper.*;
import com.tianli.dividends.DividendsService;
import com.tianli.dividends.mapper.Dividends;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.LowSettlementService;
import com.tianli.dividends.settlement.mapper.*;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.agentadmin.dto.*;
import com.tianli.management.agentadmin.mapper.AgentAdminMapper;
import com.tianli.management.agentadmin.mapper.HomeStatDataDTO;
import com.tianli.management.agentadmin.vo.*;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.tool.MapTool;
import com.tianli.tool.RateTransTool;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserIdentity;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.statistics.UserStatisticsService;
import com.tianli.user.statistics.mapper.UserStatistics;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgentAdminService {

    @Resource
    private AgentAdminMapper agentAdminMapper;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private AgentService agentService;

    @Resource
    private UserStatisticsService userStatisticsService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private ConfigService configService;

    @Resource
    private AgentTeamService agentTeamService;

    @Resource
    private ChargeDepositService chargeDepositService;

    @Resource
    private DividendsService dividendsService;

    @Resource
    private ChargeSettlementService chargeSettlementService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RebateService rebateService;

    @Resource
    private UserReferralService userReferralService;

    @Resource
    private LowDepositService lowDepositService;

    @Resource
    private LowSettlementService lowSettlementService;

    @Resource
    private CurrencyLogService currencyLogService;

    private HomeStatDataVO statLowData(long seniorId) {
        HomeStatDataDTO stat = agentAdminMapper.selectLowStatBySeniorId(seniorId);
        if (stat == null) {
            return new HomeStatDataVO();
        }
        return HomeStatDataVO.trans(stat);
    }

    public Map<String, Object> statHomeData2(){
        Long uid = requestInitService.uid();
        Agent byId = agentService.getById(uid);
        if(Objects.isNull(byId)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        UserStatistics userStatistics = userStatisticsService.get(uid);
        if (Objects.isNull(userStatistics)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }

        //返佣
        Map<String, Object> rebateMap = rebateService.getMap(new QueryWrapper<Rebate>()
                .select("ifnull(SUM(`rebate_amount`), 0) AS total")
                .eq("rebate_uid", uid));
        BigInteger rebate = ((BigDecimal)rebateMap.get("total")).toBigInteger();

        //抽水
//        Map<String, Object> rakeMap = currencyLogService.getMap(new QueryWrapper<CurrencyLog>()
//                .select("ifnull(SUM(`amount`), 0) AS total")
//                .eq("`des`", CurrencyLogDes.抽水)
//                .eq("`uid`", uid));
//        BigInteger rake = ((BigDecimal)rakeMap.get("total")).toBigInteger();

        return MapTool.Map()
                .put("team_number",byId.getSuper_agent()?userStatistics.getTeam_number():userStatistics.getReferral_number())
                .put("rebate",TokenCurrencyType.usdt_omni.money(rebate));
//                .put("rake",TokenCurrencyType.usdt_omni.money(rake));
    }

    public Map<String, Object> statHomeData() {
        Long uid = requestInitService.uid();
        Currency deposit = currencyService.get(uid, CurrencyTypeEnum.deposit);
        if (Objects.isNull(deposit)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        UserStatistics userStatistics = userStatisticsService.get(uid);
        if (Objects.isNull(userStatistics)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Agent agent = agentService.getById(uid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Currency settlement = currencyService.get(uid, CurrencyTypeEnum.settlement);
        BigInteger rebate = rebateService.totalRebateAmount(uid);
        BigInteger rebateBF = rebateService.totalRebateBFAmount(uid);
        HomeStatDataVO dataVO = HomeStatDataVO.builder()
                .balance(TokenCurrencyType.usdt_omni.money(deposit.getBalance()))
                .profit(TokenCurrencyType.usdt_omni.money(agent.getProfit()))
                .settled_number(TokenCurrencyType.usdt_omni.money(agent.getProfit().subtract(settlement.getBalance())))
                .un_settled_number(TokenCurrencyType.usdt_omni.money(settlement.getBalance()))
                .team_number(userStatistics.getTeam_number())
                .rebate(TokenCurrencyType.usdt_omni.money(rebate))
                .rebate(TokenCurrencyType.BF_bep20.money(rebateBF))
                .build();
        HomeStatDataVO homeStatDataVO = statLowData(uid);
        return MapTool.Map()
                .put("myStat", dataVO)
                .put("lowStat", homeStatDataVO);
    }

    public Map<String, Object> statTeamData(Integer page, Integer size) {
        Long uid = requestInitService.uid();
        UserStatistics userStatistics = userStatisticsService.get(uid);
        if (Objects.isNull(userStatistics)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Map<String, Object> part1 = MapTool.Map()
                .put("total", userStatistics.getTeam_number())
                .put("referral", userStatistics.getReferral_number())
                .put("low", userStatistics.getTeam_number() - userStatistics.getReferral_number());
        List<Map<String, Object>> statTeamNum = agentTeamService.statTeamNumDaily(uid);
        Agent byId = agentService.getById(uid);
        //成为代理商到今天，有多少天就多少条数据
        LocalDateTime startDateTime = TimeTool.minDay(byId.getCreate_time());
        Duration between = Duration.between(startDateTime, LocalDateTime.now());
        int totalNum = (int) between.toDays() + 1;
        String startDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(startDateTime);
        String today = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        List<Map<String, Object>> statTeamIncrementDaily = agentTeamService.statTeamIncrementDaily(uid, today, startDate, totalNum, page, size);
        for (Map<String, Object> map : statTeamIncrementDaily) {
            Long team_number = (Long) map.get("team_number");
            Long referral_number = (Long) map.get("referral_number");
            map.put("low_number", team_number - referral_number);
        }
        return MapTool.Map()
                .put("summary", part1)
                .put("overview", statTeamNum)
                .put("detail", statTeamIncrementDaily)
                .put("totalNum", totalNum);
    }

    public Map<String, Object> depositRechargePage(String txid, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        int count = chargeDepositService.count(new LambdaUpdateWrapper<ChargeDeposit>()
                .eq(ChargeDeposit::getUid, uid)
                .eq(ChargeDeposit::getCharge_type, ChargeDepositType.recharge)
                .like(StringUtils.isNotBlank(txid), ChargeDeposit::getTxid, txid)
                .ge(StringUtils.isNotBlank(startTime), ChargeDeposit::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), ChargeDeposit::getCreate_time, endTime));
        Map<String, Object> sumAmountMap = chargeDepositService.getMap(new QueryWrapper<ChargeDeposit>()
                .select("ifnull(SUM(CASE WHEN `currency_type` = 'usdt_erc20' THEN `amount` ELSE 0 END), 0) as sumAmountErc20",
                        "ifnull(SUM(CASE WHEN `currency_type` = 'usdt_omni' THEN `amount` ELSE 0 END), 0) as sumAmountOmni")
                .eq("uid", uid)
                .eq("charge_type", ChargeDepositType.recharge)
                .like(StringUtils.isNotBlank(txid), "txid", txid)
                .ge(StringUtils.isNotBlank(startTime), "create_time", startTime)
                .le(StringUtils.isNotBlank(endTime), "create_time", endTime));
        BigInteger sumAmountErc20 = ((BigDecimal) sumAmountMap.get("sumAmountErc20")).toBigInteger();
        BigInteger sumAmountOmni = ((BigDecimal) sumAmountMap.get("sumAmountOmni")).toBigInteger();
        Pair<DigitalCurrency, Double> sumAmountOmni_ = new DigitalCurrency(TokenCurrencyType.usdt_erc20, sumAmountErc20).toOtherAndPrice(TokenCurrencyType.usdt_omni);
        double sumAmount = TokenCurrencyType.usdt_omni.money(sumAmountOmni_.getValue0().getAmount().add(sumAmountOmni));
        Page<ChargeDeposit> depositRechargePage = chargeDepositService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ChargeDeposit>()
                        .eq(ChargeDeposit::getUid, uid)
                        .eq(ChargeDeposit::getCharge_type, ChargeDepositType.recharge)
                        .like(StringUtils.isNotBlank(txid), ChargeDeposit::getTxid, txid)
                        .ge(StringUtils.isNotBlank(startTime), ChargeDeposit::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ChargeDeposit::getCreate_time, endTime)
                        .orderByDesc(ChargeDeposit::getCreate_time));
        List<ChargeDeposit> records = depositRechargePage.getRecords();
        List<ChargeDepositRechargePageVO> vos = records.stream().map(ChargeDepositRechargePageVO::trans).collect(Collectors.toList());
        return MapTool.Map()
                .put("totalNum", count)
                .put("page", vos)
                .put("totalDeposit", sumAmount);
    }

    public Map<String, Object> depositWithdrawPage(String txid, String startTime, String endTime, ChargeSettlementStatus status, DepositSettlementType settlement_type, Integer page, Integer size) {
        Long uid = requestInitService.uid();

        Map<String, Object> sumAmountErc20Map = chargeDepositService.getMap(new QueryWrapper<ChargeDeposit>()
                .select("ifnull(SUM(CASE WHEN `status` = 'transacting' OR `status` = 'created' THEN `amount` ELSE 0 END), 0) as executingAmountErc20, " +
                        "ifnull(SUM(CASE WHEN `status` = 'transaction_success' THEN `amount` ELSE 0 END), 0) as sucAmountErc20")
                .eq("uid", uid)
                .eq("charge_type", ChargeDepositType.withdraw)
                .eq(Objects.nonNull(status), "status",status)
                .eq(Objects.nonNull(settlement_type), "settlement_type",settlement_type)
                .eq("currency_type",TokenCurrencyType.usdt_erc20)
                .like(StringUtils.isNotBlank(txid), "txid", txid)
                .ge(StringUtils.isNotBlank(startTime), "create_time", startTime)
                .le(StringUtils.isNotBlank(endTime), "create_time", endTime));
        BigInteger executingAmountErc20 = ((BigDecimal) sumAmountErc20Map.get("executingAmountErc20")).toBigInteger();
        BigInteger sucAmountErc20 = ((BigDecimal) sumAmountErc20Map.get("sucAmountErc20")).toBigInteger();

        Map<String, Object> sumAmountOmniMap = chargeDepositService.getMap(new QueryWrapper<ChargeDeposit>()
                .select("ifnull(SUM(CASE WHEN `status` = 'transacting' OR `status` = 'created' THEN `amount` ELSE 0 END), 0) as executingAmountOmni, " +
                        "ifnull(SUM(CASE WHEN `status` = 'transaction_success' THEN `amount` ELSE 0 END), 0) as sucAmountOmni")
                .eq("uid", uid)
                .eq("charge_type", ChargeDepositType.withdraw)
                .eq(Objects.nonNull(status), "status", status)
                .eq(Objects.nonNull(settlement_type), "settlement_type", settlement_type)
                .eq("currency_type", TokenCurrencyType.usdt_omni)
                .like(StringUtils.isNotBlank(txid), "txid", txid)
                .ge(StringUtils.isNotBlank(startTime), "create_time", startTime)
                .le(StringUtils.isNotBlank(endTime), "create_time", endTime));
        BigInteger executingAmountOmni = ((BigDecimal) sumAmountOmniMap.get("executingAmountOmni")).toBigInteger();
        BigInteger sucAmountOmni = ((BigDecimal) sumAmountOmniMap.get("sucAmountOmni")).toBigInteger();

        BigInteger executingAmount = executingAmountOmni.add(executingAmountErc20.multiply(new BigInteger("100")));
        BigInteger sucAmount = sucAmountOmni.add(sucAmountErc20.multiply(new BigInteger("100")));

        Page<ChargeDeposit> depositWithdrawPage = chargeDepositService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ChargeDeposit>()
                        .eq(ChargeDeposit::getUid, uid)
                        .eq(ChargeDeposit::getCharge_type, ChargeDepositType.withdraw)
                        .eq(Objects.nonNull(status), ChargeDeposit::getStatus, status)
                        .eq(Objects.nonNull(settlement_type), ChargeDeposit::getSettlement_type, settlement_type)
                        .like(StringUtils.isNotBlank(txid), ChargeDeposit::getTxid, txid)
                        .ge(StringUtils.isNotBlank(startTime), ChargeDeposit::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ChargeDeposit::getCreate_time, endTime)
                        .orderByDesc(ChargeDeposit::getCreate_time));
        long count = depositWithdrawPage.getTotal();
        List<ChargeDeposit> records = depositWithdrawPage.getRecords();
        List<ChargeDepositWithdrawPageVO> vos = records.stream().map(ChargeDepositWithdrawPageVO::trans).collect(Collectors.toList());
        return MapTool.Map()
                .put("totalNum", count)
                .put("executingAmount", TokenCurrencyType.usdt_omni.money(executingAmount))
                .put("sucAmount", TokenCurrencyType.usdt_omni.money(sucAmount))
                .put("page", vos);
    }

    public Map<String, Object> getDepositWithdraw() {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Agent agent = agentService.getById(uid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        String usdt_erc20_agent_withdraw_rate = configService.get(ConfigConstants.USDT_ERC20_AGENT_WITHDRAW_RATE);
        String usdt_erc20_agent_withdraw_fixed_amount = configService.get(ConfigConstants.USDT_ERC20_AGENT_WITHDRAW_FIXED_AMOUNT);
        String usdt_erc20_agent_withdraw_min_amount = configService.get(ConfigConstants.USDT_ERC20_AGENT_WITHDRAW_MIN_AMOUNT);
        String usdt_omni_agent_withdraw_fixed_amount = configService.get(ConfigConstants.USDT_OMNI_AGENT_WITHDRAW_FIXED_AMOUNT);
        String usdt_omni_agent_withdraw_min_amount = configService.get(ConfigConstants.USDT_OMNI_AGENT_WITHDRAW_MIN_AMOUNT);
        String usdt_omni_agent_withdraw_rate = configService.get(ConfigConstants.USDT_OMNI_AGENT_WITHDRAW_RATE);
        return MapTool.Map()
                .put("balanceErc20", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("remainErc20", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("balanceOmni", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("remainOmni", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("usdt_erc20_agent_withdraw_rate", Double.valueOf(RateTransTool.multi(usdt_erc20_agent_withdraw_rate)))
                .put("usdt_erc20_agent_withdraw_fixed_amount", TokenCurrencyType.usdt_erc20.money(new BigInteger(usdt_erc20_agent_withdraw_fixed_amount)))
                .put("usdt_erc20_agent_withdraw_min_amount", TokenCurrencyType.usdt_erc20.money(new BigInteger(usdt_erc20_agent_withdraw_min_amount)))
                .put("usdt_omni_agent_withdraw_fixed_amount", TokenCurrencyType.usdt_omni.money(new BigInteger(usdt_omni_agent_withdraw_fixed_amount)))
                .put("usdt_omni_agent_withdraw_min_amount", TokenCurrencyType.usdt_omni.money(new BigInteger(usdt_omni_agent_withdraw_min_amount)))
                .put("usdt_omni_agent_withdraw_rate", Double.valueOf(RateTransTool.multi(usdt_omni_agent_withdraw_rate)));
    }

    @Transactional
    public void postDepositWithdraw(Double withdrawAmount, String toAddress, String note, TokenCurrencyType currencyType) {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        BigInteger amount = currencyType.amount(withdrawAmount);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Agent agent = agentService.getById(uid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (agent.getSenior_id() != 0L) ErrorCodeEnum.ACCESS_DENY.throwException();
        if (currency.getRemain().compareTo(amount) < 0) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        String from_address = null;
        switch (currencyType) {
            case usdt_omni:
                from_address = configService.get("btc_address");
                break;
            case usdt_erc20:
                from_address = configService.get("eth_address");
                break;
        }
        if (from_address == null) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }

        String agent_withdraw_min_amount = configService._get(currencyType.name() + "_agent_withdraw_min_amount");
        if (amount.compareTo(new BigInteger(agent_withdraw_min_amount)) < 0) {
            ErrorCodeEnum.throwException("撤回保证金数额小于最低撤回数额");
        }
        String rate = configService._get(currencyType.name() + "_agent_withdraw_rate");
        String fixedAmount = configService._get(currencyType.name() + "_agent_withdraw_fixed_amount");
        BigInteger fee = (new BigDecimal(amount).multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0")))
                .toBigInteger().add(new BigInteger(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));

        BigInteger real_amount = amount.subtract(fee);
        if (fee.compareTo(BigInteger.ZERO) < 0)
            ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (real_amount.compareTo(BigInteger.ZERO) < 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();

        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        long generalId = CommonFunction.generalId();
        String generalSn = CommonFunction.generalSn(generalId);
        ChargeDeposit chargeDeposit = ChargeDeposit.builder()
                .id(generalId)
                .create_time(LocalDateTime.now())
                .status(ChargeDepositStatus.created)
                .uid(uid)
                .uid_avatar(userInfo.getAvatar())
                .uid_username(userInfo.getUsername())
                .uid_nick(agent.getNick())
                .sn("CD" + generalSn)
                .currency_type(currencyType)
                .charge_type(ChargeDepositType.withdraw)
                .settlement_type(DepositSettlementType.chain)
                .amount(amount)
                .fee(fee)
                .real_amount(real_amount)
                .from_address(from_address)
                .to_address(toAddress)
                .note(note).build();
        if(!chargeDepositService.save(chargeDeposit)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        if (currencyType == TokenCurrencyType.usdt_erc20) {
            amount = amount.multiply(new BigInteger("100"));
        }
        currencyService.freeze(uid, CurrencyTypeEnum.deposit, amount, chargeDeposit.getSn(), CurrencyLogDes.撤回.name());

    }

    public Map<String, Object> dividendsPage(String phone, BetResultEnum result,
                                             String startTime, String endTime,
                                             Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Page<Dividends> dividendsPage = dividendsService.page(new Page<>(page, size), new LambdaQueryWrapper<Dividends>()
                .like(Dividends::getDividends_uid, uid)
                .like(StringUtils.isNotBlank(phone), Dividends::getUid_username, phone)
                .eq(Objects.nonNull(result), Dividends::getResult, result)
                .ge(StringUtils.isNotBlank(startTime), Dividends::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Dividends::getCreate_time, endTime)
                .orderByDesc(Dividends::getCreate_time)
        );
        long total = dividendsPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("stat", MapTool.Map());
        }
        List<Dividends> records = dividendsPage.getRecords();
        List<SeniorDividendsVO> vos = records.stream().map(SeniorDividendsVO::trans).collect(Collectors.toList());
        // 统计汇总数据
        Map<String, BigDecimal> stat = agentAdminMapper.selectSumDividends(uid, phone, result, startTime, endTime);
        BigInteger amountSum = stat.get("amountSum").toBigInteger();
        BigInteger myProfitSumBF = stat.get("myProfitSumBF").toBigInteger();
        BigInteger myProfitSum = stat.get("myProfitSum").toBigInteger();
        double amountSumDouble = TokenCurrencyType.usdt_omni.money(amountSum);
        double myProfitSumBFDouble = TokenCurrencyType.BF_bep20.money(myProfitSumBF);
        double myProfitSumDouble = TokenCurrencyType.usdt_omni.money(myProfitSum);
        MapTool mapTool = MapTool.Map().put("amountSum", amountSumDouble).put("myProfitSumBF", myProfitSumBFDouble).put("myProfitSum", myProfitSumDouble);
        return MapTool.Map()
                .put("total", total)
                .put("list", vos)
                .put("stat", mapTool);
    }

    public Map<String, Object> settlementRechargePage(String txid, DepositSettlementType type, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Page<ChargeSettlement> settlementPage = chargeSettlementService.page(new Page<>(page, size), new LambdaQueryWrapper<ChargeSettlement>()
                .eq(ChargeSettlement::getUid, uid)
                .eq(ChargeSettlement::getCharge_type, ChargeSettlementType.recharge)
                .eq(Objects.nonNull(type), ChargeSettlement::getSettlement_type, type)
                .like(StringUtils.isNotBlank(txid), ChargeSettlement::getTxid, txid)
                .ge(StringUtils.isNotBlank(startTime), ChargeSettlement::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), ChargeSettlement::getCreate_time, endTime)
                .orderByDesc(ChargeSettlement::getCreate_time)
        );
        long total = settlementPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList());
        }
        List<ChargeSettlement> records = settlementPage.getRecords();
        return MapTool.Map()
                .put("total", total)
                .put("list", records.stream().map(ChargeSettlementRechargeVO::trans).collect(Collectors.toList()));
    }

    public Map<String, Object> settlementWithdrawPage(String txid, ChargeSettlementStatus status, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Page<ChargeSettlement> settlementPage = chargeSettlementService.page(new Page<>(page, size), new LambdaQueryWrapper<ChargeSettlement>()
                .eq(ChargeSettlement::getUid, uid)
                .eq(ChargeSettlement::getCharge_type, ChargeSettlementType.withdraw)
                .eq(Objects.nonNull(status), ChargeSettlement::getStatus, status)
                .like(StringUtils.isNotBlank(txid), ChargeSettlement::getTxid, txid)
                .ge(StringUtils.isNotBlank(startTime), ChargeSettlement::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), ChargeSettlement::getCreate_time, endTime)
                .orderByDesc(ChargeSettlement::getCreate_time)
        );
        long total = settlementPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList());
        }
        List<ChargeSettlement> records = settlementPage.getRecords();
        return MapTool.Map()
                .put("total", total)
                .put("list", records.stream().map(SeniorChargeSettlementVO::trans).collect(Collectors.toList()));
    }

    public Map<String, Object> settlementMyCurrency() {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.settlement);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }

        MapTool settleConfig = getSettleConfig();
        return MapTool.Map()
                .put("balance", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("remain", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("balanceBF", TokenCurrencyType.BF_bep20.money(currency.getBalance_BF()))
                .put("remainBF", TokenCurrencyType.BF_bep20.money(currency.getRemain_BF()))
                .put("settleConfig", settleConfig);
    }

    private MapTool getSettleConfig() {
//        String bep20SettleRate = configService.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_RATE);
//        String bep20SettleFixedAmount = configService.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_FIXED_AMOUNT);
//        String bep20SettleMinAmount = configService.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_MIN_AMOUNT);
        String trc20SettleRate = configService.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_RATE);
        String trc20SettleFixedAmount = configService.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_FIXED_AMOUNT);
        String trc20SettleMinAmount = configService.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_MIN_AMOUNT);
        String BFSettleRate = configService.get(ConfigConstants.BF_AGENT_SETTLE_RATE);
        String BFSettleFixedAmount = configService.get(ConfigConstants.BF_AGENT_SETTLE_FIXED_AMOUNT);
        String BFSettleMinAmount = configService.get(ConfigConstants.BF_AGENT_SETTLE_MIN_AMOUNT);
        return MapTool.Map()
//                .put("bep20", MapTool.Map()
//                        .put("rate", Double.valueOf(RateTransTool.multi(bep20SettleRate)))
//                        .put("fixedAmount", TokenCurrencyType.usdt_bep20.money(new BigInteger(bep20SettleFixedAmount)))
//                        .put("minAmount", TokenCurrencyType.usdt_bep20.money(new BigInteger(bep20SettleMinAmount)))
//                )
                .put("trc20", MapTool.Map()
                        .put("rate", Double.valueOf(RateTransTool.multi(trc20SettleRate)))
                        .put("fixedAmount", TokenCurrencyType.usdt_trc20.money(new BigInteger(trc20SettleFixedAmount)))
                        .put("minAmount", TokenCurrencyType.usdt_trc20.money(new BigInteger(trc20SettleMinAmount)))
                )
                .put("BF", MapTool.Map()
                        .put("rate", Double.valueOf(RateTransTool.multi(BFSettleRate)))
                        .put("fixedAmount", TokenCurrencyType.BF_bep20.money(new BigInteger(BFSettleFixedAmount)))
                        .put("minAmount", TokenCurrencyType.BF_bep20.money(new BigInteger(BFSettleMinAmount)))
                );
    }

    @Transactional
    public void settlementWithdrawApply(SeniorSettlementDTO dto) {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.settlement);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Agent agent = agentService.getById(uid);
        if (agent == null) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        Double amount = dto.getAmount();
        TokenCurrencyType currencyType = dto.getCurrencyType();
        BigDecimal bigAmount = new BigDecimal(currencyType.amount(amount));
        if (bigAmount.compareTo(BigDecimal.ZERO) <= 0) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        if (Objects.isNull(userInfo)) {
            ErrorCodeEnum.ACCOUNT_BAND.throwException();
        }
        if(TokenCurrencyType.usdt_trc20 == currencyType || TokenCurrencyType.usdt_bep20 == currencyType) {
            if (TokenCurrencyType.usdt_omni.money(currency.getRemain()) < amount) {
                ErrorCodeEnum.CREDIT_LACK.throwException();
            }
        } else {
            if(TokenCurrencyType.BF_bep20.money(currency.getRemain_BF()) < amount) {
                ErrorCodeEnum.CREDIT_LACK.throwException();
            }
        }
        BigInteger fixedAmount;
        double rate;
        String fixedAmountConfig = null;
        String rateConfig = null;
        String minAmount = null;
        String from_address = null;
        CurrencyTokenEnum token = CurrencyTokenEnum.usdt_omni;
        switch (currencyType) {
            case usdt_trc20:
                from_address = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                fixedAmountConfig = configService.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_FIXED_AMOUNT);
                rateConfig = configService.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_RATE);
                minAmount = configService.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_MIN_AMOUNT);
                break;
            case BF_bep20:
                from_address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                fixedAmountConfig = configService.get(ConfigConstants.BF_AGENT_SETTLE_FIXED_AMOUNT);
                rateConfig = configService.get(ConfigConstants.BF_AGENT_SETTLE_RATE);
                minAmount = configService.get(ConfigConstants.BF_AGENT_SETTLE_MIN_AMOUNT);
                token = CurrencyTokenEnum.BF_bep20;
                break;
            case usdt_bep20:
                from_address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                fixedAmountConfig = configService.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_FIXED_AMOUNT);
                rateConfig = configService.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_RATE);
                minAmount = configService.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_MIN_AMOUNT);
                break;
        }
        if (from_address == null || fixedAmountConfig == null || rateConfig == null || minAmount == null) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        if (bigAmount.compareTo(new BigDecimal(minAmount)) < 0) {
            ErrorCodeEnum.throwException("结算金额小于最小结算金额" + currencyType.money(new BigInteger(minAmount)) + "USDT");
        }
        fixedAmount = new BigInteger(fixedAmountConfig);
        rate = Double.parseDouble(rateConfig);
        BigInteger fee = fixedAmount.add(bigAmount.multiply(new BigDecimal("" + rate)).toBigInteger());
        BigInteger real_amount = bigAmount.toBigInteger().subtract(fee);
        if (fee.compareTo(BigInteger.ZERO) < 0)
            ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (real_amount.compareTo(BigInteger.ZERO) <= 0) {
            ErrorCodeEnum.throwException("结算金额必须大于手续费");
        }
        long generalId = CommonFunction.generalId();
        ChargeSettlement settlement = ChargeSettlement.builder()
                .id(generalId)
                .create_time(LocalDateTime.now())
                .status(ChargeSettlementStatus.created)
                .uid(uid)
                .uid_username(userInfo.getUsername())
                .uid_nick(agent.getNick())
                .uid_avatar(userInfo.getAvatar())
                .sn("CS" + CommonFunction.generalSn(generalId))
                .currency_type(currencyType)
                .charge_type(ChargeSettlementType.withdraw)
                .settlement_type(DepositSettlementType.chain)
                .amount(bigAmount.toBigInteger())
                .fee(fee)
                .from_address(from_address)
                .real_amount(real_amount)
                .to_address(dto.getToAddress())
                .note(dto.getNote())
                .build();
        boolean save = chargeSettlementService.save(settlement);
        if (!save) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        BigInteger bigAmountBigInteger = bigAmount.toBigInteger();
        if (currencyType == TokenCurrencyType.usdt_trc20) {
            bigAmountBigInteger = bigAmountBigInteger.multiply(ChargeService.ONE_HUNDRED);
        } else if(currencyType == TokenCurrencyType.usdt_bep20) {
            bigAmountBigInteger = bigAmountBigInteger.divide(ChargeService.TEN_BILLION);
        }
        currencyService.freeze(uid, CurrencyTypeEnum.settlement, token, bigAmountBigInteger, settlement.getSn(), CurrencyLogDes.结算.name());
    }


    public Map<String, Object> rebatePage(String phone, Long bet_id, String startTime, String endTime, Integer page, Integer size) {
        Long agentId = requestInitService.uid();
        LambdaQueryWrapper<Rebate> wrapper = new LambdaQueryWrapper<Rebate>()
                .eq(Rebate::getRebate_uid, agentId)
                .like(StringUtils.isNotBlank(phone), Rebate::getUid_username, phone)
                .like(Objects.nonNull(bet_id), Rebate::getBet_id, bet_id)
                .ge(StringUtils.isNotBlank(startTime), Rebate::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Rebate::getCreate_time, endTime)
                .orderByDesc(Rebate::getId);

        Page<Rebate> rebatePage = rebateService.page(new Page<>(page, size), wrapper);
        List<Rebate> records = rebatePage.getRecords();
        for (Rebate record : records) {
            Long uid = record.getUid();
            //是否为直邀
            UserReferral one = userReferralService.getOne(new LambdaQueryWrapper<UserReferral>()
                    .eq(UserReferral::getReferral_id, agentId)
                    .eq(UserReferral::getId, uid));
            if (one == null) {
                String uid_username = record.getUid_username();
                String resultPhone = uid_username.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                record.setUid_username(resultPhone);
            }
        }
        List<AgentRebatePageVO> vos = records.stream().map(AgentRebatePageVO::trans).collect(Collectors.toList());

        return MapTool.Map().put("totalNum", rebatePage.getTotal()).put("page", vos);
    }

    public Map<String, Object> getLowAgentDepositPage(Long id, LowDepositChargeType type, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(id);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(agent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        Currency currency = currencyService.get(id, CurrencyTypeEnum.deposit);
        LambdaQueryWrapper<LowDeposit> queryWrapper = new LambdaQueryWrapper<LowDeposit>()
                .eq(LowDeposit::getLow_uid, id)
                .eq(Objects.nonNull(type), LowDeposit::getCharge_type, type)
                .ge(StringUtils.isNotBlank(startTime), LowDeposit::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), LowDeposit::getCreate_time, endTime)
                .orderByDesc(LowDeposit::getId);
        Page<LowDeposit> lowSettlementPage = lowDepositService.page(new Page<>(page, size), queryWrapper);
        long total = lowSettlementPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("balance", Objects.isNull(currency) ? 0 : TokenCurrencyType.usdt_omni.money(currency.getBalance()));
        }
        List<LowDeposit> records = lowSettlementPage.getRecords();
        List<SeniorLowLowDepositVO> vos = records.stream().map(SeniorLowLowDepositVO::trans).collect(Collectors.toList());
        return MapTool.Map()
                .put("total", total)
                .put("list", vos)
                .put("balance", Objects.isNull(currency) ? 0 : TokenCurrencyType.usdt_omni.money(currency.getBalance()));
    }

    @Transactional
    public void saveLowAgentSettle(LowSettlementDTO dto) {
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(dto.getAmount());
        if (amount.compareTo(BigInteger.ZERO) <= 0) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        long uid = requestInitService.uid();
        redisLock.lock("saveLowAgentSettle_" + uid, 1L, TimeUnit.MINUTES);
        long id = dto.getId();
        Agent agent = agentService.getById(id);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(agent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        LowSettlementChargeType type = dto.getType();
        long generalId = CommonFunction.generalId();
        if (type.equals(LowSettlementChargeType.transfer_out)) {
            currencyService.lowWithdraw(id, CurrencyTypeEnum.settlement, amount, String.format("low_settlement_%d", generalId), CurrencyLogDes.转出.name());
        } else {
            currencyService.increase(id, CurrencyTypeEnum.settlement, amount, String.format("low_settlement_%d", generalId), CurrencyLogDes.转入.name());
        }

        LowSettlement settlement = LowSettlement.builder()
                .id(generalId)
                .create_time(LocalDateTime.now())
                .senior_uid(uid)
                .low_uid(id)
                .amount(amount)
                .charge_type(type)
                .note(dto.getNote())
                .build();
        if (!lowSettlementService.save(settlement)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
    }

    @Transactional
    public void updateLowAgentSettle(Long id, LowSettlementDTO dto) {
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(dto.getAmount());
        if (amount.compareTo(BigInteger.ZERO) <= 0) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        long uid = requestInitService.uid();
        redisLock.lock("updateLowAgentSettle_" + id, 1L, TimeUnit.MINUTES);
        long lowUid = dto.getId();
        Agent agent = agentService.getById(lowUid);
        if (Objects.isNull(agent) || !Objects.equals(agent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        LowSettlement byId = lowSettlementService.getById(id);
        if (Objects.isNull(byId) || !Objects.equals(byId.getLow_uid(), lowUid) || !Objects.equals(byId.getSenior_uid(), uid)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (byId.getCharge_type().equals(LowSettlementChargeType.transfer_out)) {
            currencyService.increase(lowUid, CurrencyTypeEnum.settlement, byId.getAmount(), String.format("low_settlement_%d", byId.getId()), CurrencyLogDes.转入.name());
        } else {
            currencyService.lowWithdraw(lowUid, CurrencyTypeEnum.settlement, byId.getAmount(), String.format("low_settlement_%d", byId.getId()), CurrencyLogDes.转出.name());
        }

        LowSettlementChargeType type = dto.getType();
        if (type.equals(LowSettlementChargeType.transfer_out)) {
            currencyService.lowWithdraw(lowUid, CurrencyTypeEnum.settlement, amount, String.format("low_settlement_%d", byId.getId()), CurrencyLogDes.转出.name());
        } else {
            currencyService.increase(lowUid, CurrencyTypeEnum.settlement, amount, String.format("low_settlement_%d", byId.getId()), CurrencyLogDes.转入.name());
        }

        LowSettlement settlement = LowSettlement.builder()
                .id(byId.getId())
                .amount(amount)
                .charge_type(type)
                .note(dto.getNote())
                .build();
        if (!lowSettlementService.updateById(settlement)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
    }

    @Transactional
    public void deleteLowAgentSettle(long id) {
        redisLock.lock("updateLowAgentSettle_" + id, 1L, TimeUnit.MINUTES);
        long uid = requestInitService.uid();
        LowSettlement byId = lowSettlementService.getById(id);
        if (Objects.isNull(byId)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        if (!Objects.equals(byId.getSenior_uid(), uid)) ErrorCodeEnum.ACCESS_DENY.throwException();
        boolean removeById = lowSettlementService.removeById(id);
        if (!removeById) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        if (byId.getCharge_type().equals(LowSettlementChargeType.transfer_into)) {
            currencyService.lowWithdraw(byId.getLow_uid(), CurrencyTypeEnum.settlement, byId.getAmount(), String.format("low_settlement_%d", byId.getId()), CurrencyLogDes.转出.name());
        } else {
            currencyService.increase(byId.getLow_uid(), CurrencyTypeEnum.settlement, byId.getAmount(), String.format("low_settlementS_%d", byId.getId()), CurrencyLogDes.转入.name());
        }
    }

    @Transactional
    public void saveLowAgentDeposit(LowDepositDTO dto) {
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(dto.getAmount());

        if (amount.compareTo(BigInteger.ZERO) <= 0) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        long uid = requestInitService.uid();
        redisLock.lock("saveLowAgentDeposit_" + uid, 1L, TimeUnit.MINUTES);
        long id = dto.getId();
        Agent agent = agentService.getById(id);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(agent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }

        long generalId = CommonFunction.generalId();
        LowDepositChargeType type = dto.getType();
        if (LowDepositChargeType.withdraw == type) {
            currencyService.lowWithdraw(id, CurrencyTypeEnum.deposit, amount, String.format("low_deposit_%d", generalId), CurrencyLogDes.撤回.name());
        } else {
            currencyService.increase(id, CurrencyTypeEnum.deposit, amount, String.format("low_deposit_%d", generalId), CurrencyLogDes.缴纳.name());
        }

        LowDeposit deposit = LowDeposit.builder()
                .id(generalId)
                .create_time(LocalDateTime.now())
                .senior_uid(uid)
                .low_uid(id)
                .amount(amount)
                .charge_type(type)
                .note(dto.getNote())
                .build();
        if (!lowDepositService.save(deposit)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
    }

    @Transactional
    public void updateLowAgentDeposit(Long id, LowDepositDTO dto) {
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(dto.getAmount());
        if (amount.compareTo(BigInteger.ZERO) <= 0) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        long uid = requestInitService.uid();
        redisLock.lock("updateLowAgentDeposit_" + id, 1L, TimeUnit.MINUTES);
        long lowUid = dto.getId();
        Agent agent = agentService.getById(lowUid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(agent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        LowDeposit dbLowDeposit = lowDepositService.getById(id);
        if (Objects.isNull(dbLowDeposit) || !Objects.equals(dbLowDeposit.getLow_uid(), lowUid) || !Objects.equals(dbLowDeposit.getSenior_uid(), uid)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (dbLowDeposit.getCharge_type().equals(LowDepositChargeType.withdraw)) {
            currencyService.increase(lowUid, CurrencyTypeEnum.deposit, dbLowDeposit.getAmount(), String.format("low_settlement_%d", dbLowDeposit.getId()), CurrencyLogDes.缴纳.name());
        } else {
            currencyService.lowWithdraw(lowUid, CurrencyTypeEnum.deposit, dbLowDeposit.getAmount(), String.format("low_settlement_%d", dbLowDeposit.getId()), CurrencyLogDes.撤回.name());
        }

        LowDepositChargeType type = dto.getType();
        if (LowDepositChargeType.withdraw.equals(type)) {
            currencyService.lowWithdraw(lowUid, CurrencyTypeEnum.deposit, amount, String.format("low_deposit_%d", dbLowDeposit.getId()), CurrencyLogDes.撤回.name());
        } else {
            currencyService.increase(lowUid, CurrencyTypeEnum.deposit, amount, String.format("low_deposit_%d", dbLowDeposit.getId()), CurrencyLogDes.缴纳.name());
        }

        LowDeposit deposit = LowDeposit.builder()
                .id(dbLowDeposit.getId())
                .amount(amount)
                .charge_type(type)
                .note(dto.getNote())
                .build();
        if (!lowDepositService.updateById(deposit)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
    }

    @Transactional
    public void deleteLowAgentDeposit(long id) {
        redisLock.lock("updateLowAgentDeposit_" + id, 1L, TimeUnit.MINUTES);
        long uid = requestInitService.uid();
        LowDeposit lowDeposit = lowDepositService.getById(id);
        if (Objects.isNull(lowDeposit)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        if (!Objects.equals(lowDeposit.getSenior_uid(), uid)) ErrorCodeEnum.ACCESS_DENY.throwException();
        boolean removeById = lowDepositService.removeById(id);
        if (!removeById) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        if (lowDeposit.getCharge_type().equals(LowDepositChargeType.recharge)) {
            currencyService.lowWithdraw(lowDeposit.getLow_uid(), CurrencyTypeEnum.deposit, lowDeposit.getAmount(), String.format("low_deposit_%d", lowDeposit.getId()), CurrencyLogDes.撤回.name());
        } else {
            currencyService.increase(lowDeposit.getLow_uid(), CurrencyTypeEnum.deposit, lowDeposit.getAmount(), String.format("low_deposit_%d", lowDeposit.getId()), CurrencyLogDes.缴纳.name());
        }
    }

    public Map<String, Object> getLowAgentSettlePage(Long id, LowSettlementChargeType type, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(id);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(agent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        Currency currency = currencyService.get(id, CurrencyTypeEnum.settlement);
        LambdaQueryWrapper<LowSettlement> queryWrapper = new LambdaQueryWrapper<LowSettlement>()
                .eq(LowSettlement::getLow_uid, id)
                .eq(Objects.nonNull(type), LowSettlement::getCharge_type, type)
                .ge(StringUtils.isNotBlank(startTime), LowSettlement::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), LowSettlement::getCreate_time, endTime)
                .orderByDesc(LowSettlement::getId);
        Page<LowSettlement> lowSettlementPage = lowSettlementService.page(new Page<>(page, size), queryWrapper);
        long total = lowSettlementPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("not_settled_number", Objects.isNull(currency) ? 0 : TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                    .put("stat", 0);
        }
        QueryWrapper<LowSettlement> select = new QueryWrapper<LowSettlement>()
                .select("ifnull(SUM(CASE WHEN `charge_type` = 'transfer_into' THEN `amount` ELSE 0 END), 0) as sumInAmount",
                        "ifnull(SUM(CASE WHEN `charge_type` = 'transfer_out' THEN `amount` ELSE 0 END), 0) as sumOutAmount")
                .eq("`low_uid`", id)
                .eq(Objects.nonNull(type), "`charge_type`", type)
                .ge(StringUtils.isNotBlank(startTime), "`create_time`", startTime)
                .le(StringUtils.isNotBlank(endTime), "`create_time`", endTime);
        Map<String, Object> one = lowSettlementService.getMap(select);
        BigInteger amount = ((BigDecimal) one.get("sumOutAmount")).toBigInteger().subtract(((BigDecimal) one.get("sumInAmount")).toBigInteger());
        List<LowSettlement> records = lowSettlementPage.getRecords();
        List<SeniorLowSettlementVO> vos = records.stream().map(SeniorLowSettlementVO::trans).collect(Collectors.toList());
        return MapTool.Map()
                .put("total", total)
                .put("list", vos)
                .put("not_settled_number", Objects.isNull(currency) ? 0 : TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("stat", TokenCurrencyType.usdt_omni.money(amount));
    }

    @Transactional
    public void deleteAgent(Long id) {
        Long uid = requestInitService.uid();
        Agent byId = agentService.getById(id);
        if (Objects.isNull(byId)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (byId.getIdentity() != UserIdentity.low_agent || !Objects.equals(byId.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }

        Currency settlementCurrency = currencyService.get(id, CurrencyTypeEnum.settlement);
        Currency depositCurrency = currencyService.get(id, CurrencyTypeEnum.deposit);
        if (!((settlementCurrency == null || settlementCurrency.getBalance() == null || BigInteger.ZERO.equals(settlementCurrency.getBalance()))
                && (depositCurrency == null || depositCurrency.getBalance() == null || BigInteger.ZERO.equals(depositCurrency.getBalance())))) {
            ErrorCodeEnum.throwException("删除失败，入驻金额或未结算金额不为0");
        }
        int count = agentService.count(new LambdaQueryWrapper<Agent>().eq(Agent::getSenior_id, id));
        if (count > 0) {
            ErrorCodeEnum.EXIST_LOW_AGENT.throwException();
        }
        if (!agentService.removeById(id)) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        List<UserReferral> list = userReferralService.list(new LambdaQueryWrapper<UserReferral>().eq(UserReferral::getReferral_id, id));
        List<Long> uidList = list.stream().map(UserReferral::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(uidList)) {
            boolean remove = agentTeamService.remove(new LambdaQueryWrapper<AgentTeam>().in(AgentTeam::getUid, uidList));
            if (!remove) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        boolean updateIdentity = userService.updateIdentityById(UserIdentity.normal, id);
        if (!updateIdentity) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }

    }

    @Resource
    private CaptchaPhoneService captchaPhoneService;

    @Resource
    private UserService userService;

    @Resource
    private DoubleDecimalTrans doubleDecimalTrans;

    @Transactional
    public void saveLowAgent(SaveLowAgentDTO dto) {
        Long uid = requestInitService.uid();
        String username = dto.getUsername();
        captchaPhoneService.verify(username, CaptchaPhoneType.registrationAgent, dto.getCode());
        User user = userService._getByUsername(username);
        if (Objects.isNull(user)) {
            user = userService.reg(username);
        }
        final long id = user.getId();
        Agent dbAgent = agentService.getById(id);
        if (Objects.nonNull(dbAgent)) {
            ErrorCodeEnum.REPEAT_SET_AGENT.throwException();
        }
        //1.邀请链中都没有代理商,则该用户可以被任意代理商设置为下级代理商
        //2.邀请链中有代理商,只有直邀的上级为代理商时才能设置该用户为下级代理商
        LinkedList<Long> uids = userReferralService.userReferralChain(user.getId());
        ArrayList<Agent> agentList = new ArrayList<>();
        uids.forEach(user_id->{
            Agent byId = agentService.getById(user_id);
            if(Objects.nonNull(byId)){
                agentList.add(byId);
            }
        });
        if (!CollectionUtils.isEmpty(agentList)){
            Long referralId = uids.get(uids.size()-2);
            if (!uid.equals(referralId)){
                ErrorCodeEnum.FORBID_SET_AGENT.throwException();
            }
            Agent referralAgent = agentService.getById(referralId);
            if (Objects.isNull(referralAgent)){
                ErrorCodeEnum.FORBID_SET_AGENT.throwException();
            }

        }
        //给下级代理商设置的返佣/分红占比不能超过自己的比例
        Agent seniorAgent = agentService.getById(uid);
        if (Objects.isNull(seniorAgent))ErrorCodeEnum.UNLOIGN.throwException();
        Double normal_rebate_proportion = seniorAgent.getNormal_rebate_proportion();
        Double steady_rebate_proportion = seniorAgent.getSteady_rebate_proportion();
        if (DoubleDecimalTrans.double_divide_hundred(dto.getNormal_rebate_proportion()) > normal_rebate_proportion)
            ErrorCodeEnum.throwException("普通场返佣比例应不能大于" + normal_rebate_proportion);
        if (DoubleDecimalTrans.double_divide_hundred(dto.getSteady_rebate_proportion()) > steady_rebate_proportion)
            ErrorCodeEnum.throwException("稳赚场返佣比例应不能大于" + steady_rebate_proportion);
        Double real_dividends = seniorAgent.getReal_dividends();
        if (DoubleDecimalTrans.double_divide_hundred(dto.getExpect_dividends()) > real_dividends)
            ErrorCodeEnum.throwException("分红比例应不能大于" + real_dividends);
        // 计算实际的分红比例=期望分红
//        Currency currency = currencyService.get(id, CurrencyTypeEnum.deposit);
//        double expect_dividends = 0.0;
//        double real_dividends = expect_dividends;
        // 创建agent对象
        Agent agent = Agent.builder()
                .id(id)
                .create_time(LocalDateTime.now())
                .identity(UserIdentity.low_agent)
                .senior_id(uid)
                .nick(dto.getNick())
                .username(username)
                .expect_deposit(TokenCurrencyType.usdt_omni.amount(dto.getExpect_deposit()))
                .profit(BigInteger.ZERO)
                .settled_number(BigInteger.ZERO)
                .expect_dividends(doubleDecimalTrans.double_divide_hundred(dto.getExpect_dividends()))
                .real_dividends(doubleDecimalTrans.double_divide_hundred(dto.getExpect_dividends()))
                .normal_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getNormal_rebate_proportion()))
                .steady_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getSteady_rebate_proportion()))
                .focus(false)
                .note(dto.getNote())
                .super_agent(false)
                .build();
        // 增加 代理商
        if (!agentService.save(agent)) ErrorCodeEnum.SYSTEM_ERROR.throwException();

        // 增加agentTeam数据
        List<UserReferral> userReferrals = userReferralService.list(new LambdaQueryWrapper<UserReferral>().eq(UserReferral::getReferral_id, id));
        LinkedList<Agent> agents = agentService.superiorAgentChain(id);
        if (!CollectionUtils.isEmpty(userReferrals)) {
            LocalDateTime now = LocalDateTime.now();
            List<AgentTeam> agentTeams = userReferrals.stream().map(e -> agents.stream().map(a -> AgentTeam.builder()
                    .referral_time(now)
                    .referral_id(a.getId())
                    .uid(e.getId())
                    .build()).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
            agentTeamService.saveList(agentTeams);
        }
        // 用户统计数据 更新
        List<Long> seniorIdList = agents.stream().map(Agent::getId).filter(e -> !Objects.equals(e, id)).collect(Collectors.toList());
        userStatisticsService.incrementTeamNumByIds(seniorIdList, userReferrals.size());

        boolean updateIdentity = userService.updateIdentityById(UserIdentity.low_agent, user.getId());
        if (!updateIdentity) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    public void updateLowAgent(UpdateLowAgentDTO dto) {
        Long uid = requestInitService.uid();
        String username = dto.getUsername();
        captchaPhoneService.verify(username, CaptchaPhoneType.registrationAgent, dto.getCode());
        User user = userService._getByUsername(username);
        if (Objects.isNull(user)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(user.getId(), dto.getId())) {
            ErrorCodeEnum.USER_PHONE_USED_REPEAT.throwException();
        }
        Agent dbAgent = agentService.getById(user.getId());
        if (Objects.isNull(dbAgent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (dbAgent.getIdentity() != UserIdentity.low_agent) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        if (!Objects.equals(dbAgent.getSenior_id(), uid)) {
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        //给下级代理商设置的返佣占比不能超过自己的比例
        Agent seniorAgent = agentService.getById(uid);
        if (Objects.isNull(seniorAgent))ErrorCodeEnum.UNLOIGN.throwException();
        Double normal_rebate_proportion = seniorAgent.getNormal_rebate_proportion();
        if (DoubleDecimalTrans.double_divide_hundred(dto.getNormal_rebate_proportion()) > normal_rebate_proportion)
            ErrorCodeEnum.throwException("普通场返佣比例应不能大于" + normal_rebate_proportion);
        Double steady_rebate_proportion = seniorAgent.getSteady_rebate_proportion();
        if (DoubleDecimalTrans.double_divide_hundred(dto.getSteady_rebate_proportion()) > steady_rebate_proportion)
            ErrorCodeEnum.throwException("稳赚场返佣比例应不能大于" + steady_rebate_proportion);
        Double dividends = seniorAgent.getReal_dividends();
        if (DoubleDecimalTrans.double_divide_hundred(dto.getExpect_dividends()) > dividends)
            ErrorCodeEnum.throwException("分红比例应不能大于" + dividends);
        // 计算实际的分红比例
//        Currency currency = currencyService.get(user.getId(), CurrencyTypeEnum.deposit);
        double expect_dividends = dto.getExpect_dividends();
        double real_dividends = expect_dividends;
        // 创建agent对象
        Agent agent = Agent.builder()
                .id(user.getId())
                .nick(dto.getNick())
                .username(username)
                .expect_deposit(TokenCurrencyType.usdt_omni.amount(dto.getExpect_deposit()))
                .expect_dividends(doubleDecimalTrans.double_divide_hundred(expect_dividends))
                .real_dividends(doubleDecimalTrans.double_divide_hundred(real_dividends))
                .note(dto.getNote())
                .normal_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getNormal_rebate_proportion()))
                .steady_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getSteady_rebate_proportion()))
                .build();
        if (!agentService.updateById(agent)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    public Map<String, Object> lowAgentPage2(String nick, String phone, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<Agent>()
                .eq(Agent::getSenior_id, uid)
                .like(StringUtils.isNotBlank(nick), Agent::getNick, nick)
                .like(StringUtils.isNotBlank(phone), Agent::getUsername, phone)
                .like(StringUtils.isNotBlank(startTime), Agent::getCreate_time, startTime)
                .like(StringUtils.isNotBlank(endTime), Agent::getCreate_time, endTime)
                .orderByDesc(Agent::getCreate_time);
        Page<Agent> agentPage = agentService.page(new Page<>(page, size), queryWrapper);
        long total = agentPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList());
        }
        List<Agent> records = agentPage.getRecords();
        List<Long> uids = records.stream().map(Agent::getId).collect(Collectors.toList());
        List<UserStatistics> userStatistics = userStatisticsService.list(new LambdaQueryWrapper<UserStatistics>().in(UserStatistics::getId, uids));
        Map<Long, UserStatistics> statisticsMap = userStatistics.stream().collect(Collectors.toMap(UserStatistics::getId, Function.identity()));
        List<LowAgentPageVO> lowAgentPageVOS = records.stream().map(e -> LowAgentPageVO.trans(e, statisticsMap.get(e.getId()))).collect(Collectors.toList());

        return MapTool.Map()
                .put("total", total)
                .put("list", lowAgentPageVOS);
    }

    public Map<String, Object> lowAgentPage(String nike, String phone, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<Agent>()
                .eq(Agent::getSenior_id, uid)
                .like(StringUtils.isNotBlank(nike), Agent::getNick, nike)
                .like(StringUtils.isNotBlank(phone), Agent::getUsername, phone)
                .like(StringUtils.isNotBlank(startTime), Agent::getCreate_time, startTime)
                .like(StringUtils.isNotBlank(endTime), Agent::getCreate_time, endTime)
                .orderByDesc(Agent::getCreate_time);
        Page<Agent> agentPage = agentService.page(new Page<>(page, size), queryWrapper);
        long total = agentPage.getTotal();
        if (total <= 0) {
            return MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList());
        }
        List<Agent> records = agentPage.getRecords();
        List<Long> uids = records.stream().map(Agent::getId).collect(Collectors.toList());
        List<Currency> currencies = currencyService.listByIds(uids, CurrencyTypeEnum.deposit);
        Map<Long, Currency> currencyMap = currencies.stream().collect(Collectors.toMap(Currency::getUid, Function.identity()));
        List<Currency> settlementCurrencies = currencyService.listByIds(uids, CurrencyTypeEnum.settlement);
        Map<Long, Currency> settlementCurrencyMap = settlementCurrencies.stream().collect(Collectors.toMap(Currency::getUid, Function.identity()));
        List<UserStatistics> userStatistics = userStatisticsService.list(new LambdaQueryWrapper<UserStatistics>().in(UserStatistics::getId, uids));
        Map<Long, UserStatistics> statisticsMap = userStatistics.stream().collect(Collectors.toMap(UserStatistics::getId, Function.identity()));
        List<SeniorLowAgentVO> seniorLowAgentVOS = records.stream().map(e -> SeniorLowAgentVO.trans(e, currencyMap.get(e.getId()), settlementCurrencyMap.get(e.getId()), statisticsMap.get(e.getId()))).map(e -> e.setReferral_url(invitationUrl(e.getId()))).collect(Collectors.toList());
        return MapTool.Map()
                .put("total", total)
                .put("list", seniorLowAgentVOS);
    }

    public Map<String, Object> lowAgentSettlePage(LowSettlementChargeType chargeType, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(uid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        QueryWrapper<LowSettlement> wrapper = new QueryWrapper<LowSettlement>()
                .eq("low_uid", uid)
                .eq(Objects.nonNull(chargeType), "charge_type", chargeType)
                .ge(StringUtils.isNotBlank(startTime), "create_time", startTime)
                .le(StringUtils.isNotBlank(endTime), "create_time", endTime);
        Map<String, Object> one = lowSettlementService.getMap(wrapper
                .select("ifnull(SUM(CASE WHEN `charge_type` = 'transfer_into' THEN `amount` ELSE 0 END), 0) as sumInAmount",
                        "ifnull(SUM(CASE WHEN `charge_type` = 'transfer_out' THEN `amount` ELSE 0 END), 0) as sumOutAmount"));
        BigInteger amount = ((BigDecimal) one.get("sumOutAmount")).toBigInteger().subtract(((BigDecimal) one.get("sumInAmount")).toBigInteger());
        Page<LowSettlement> settlementPage = lowSettlementService.page(new Page<>(page, size),
                new LambdaQueryWrapper<LowSettlement>().eq(LowSettlement::getLow_uid, uid)
                        .eq(Objects.nonNull(chargeType), LowSettlement::getCharge_type, chargeType)
                        .ge(StringUtils.isNotBlank(startTime), LowSettlement::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), LowSettlement::getCreate_time, endTime)
                        .orderByDesc(LowSettlement::getCreate_time));
        long total = settlementPage.getTotal();
        List<LowSettlement> records = settlementPage.getRecords();
        List<LowSettlementPageVO> vos = records.stream().map(LowSettlementPageVO::trans).collect(Collectors.toList());
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.settlement);
        return MapTool.Map()
                .put("sumAmount", TokenCurrencyType.usdt_omni.money(amount))
                .put("unsettlement", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("totalNum", total)
                .put("page", vos);
    }

    public Map<String, Object> lowAgentDepositPage(String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();

        Page<LowDeposit> pageMaps = lowDepositService.page(new Page<>(page, size),
                new LambdaQueryWrapper<LowDeposit>()
                        .select(LowDeposit::getId, LowDeposit::getAmount, LowDeposit::getCharge_type,
                                LowDeposit::getCreate_time, LowDeposit::getNote)
                        .eq(LowDeposit::getLow_uid, uid)
                        .ge(StringUtils.isNotBlank(startTime), LowDeposit::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), LowDeposit::getCreate_time, endTime)
                        .orderByDesc(LowDeposit::getId));
        long total = pageMaps.getTotal();
        List<LowDeposit> records = pageMaps.getRecords();
        List<LowDepositPageVO> vos = records.stream().map(LowDepositPageVO::trans).collect(Collectors.toList());
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        return MapTool.Map()
                .put("totalNum", total)
                .put("sumAmount", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("page", vos);
    }

    public Map<String, Object> rakeRecord(String phone, String bet_id, String startTime, String endTime, Integer page, Integer size) {
        Long uid = requestInitService.uid();
        long count = currencyLogService.rakeRecordCount(uid, phone, bet_id, startTime, endTime);
        List<RakeRecordDTO> dto = currencyLogService.rakeRecordList(uid, phone, bet_id, startTime, endTime, page, size);
        for (RakeRecordDTO e : dto) {
            Long userId = e.getUid();
            //是否为直邀
            UserReferral one = userReferralService.getOne(new LambdaQueryWrapper<UserReferral>()
                    .eq(UserReferral::getReferral_id, uid)
                    .eq(UserReferral::getId, userId));
            if (one == null) {
                String username = e.getUsername();
                String resultPhone = username.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                e.setUsername(resultPhone);
            }
        }
        List<RakeRecordVO> vos = dto.stream().map(RakeRecordVO::trans).collect(Collectors.toList());
        return MapTool.Map().put("totalNums",count).put("page",vos);
    }

    /**
     * 推广配置
     */
    public MapTool promotionConfiguration(){
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(uid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Double normal_rebate_proportion = agent.getNormal_rebate_proportion();
        Double steady_rebate_proportion = agent.getSteady_rebate_proportion();
        //给直邀设置的返佣比例
        Double invite_normal_rebate_proportion = agent.getInvite_normal_rebate_proportion();
        Double invite_steady_rebate_proportion = agent.getInvite_steady_rebate_proportion();
        //邀请链接
        String referral_url = invitationUrl(uid);
        return MapTool.Map().put("invite_normal_rebate_proportion", DoubleDecimalTrans.double_multiply_hundred(invite_normal_rebate_proportion))
                .put("invite_steady_rebate_proportion", DoubleDecimalTrans.double_multiply_hundred(invite_steady_rebate_proportion))
                .put("referral_url", referral_url)
                .put("normal_rebate_proportion", DoubleDecimalTrans.double_multiply_hundred(normal_rebate_proportion))
                .put("steady_rebate_proportion", steady_rebate_proportion == 0.0 ? DoubleDecimalTrans.double_multiply_hundred(normal_rebate_proportion) : DoubleDecimalTrans.double_multiply_hundred(steady_rebate_proportion));
    }

    /**
     * 获取邀请链接
     */
    public String invitationUrl(Long uid){
        String url = configService.get("url");
        String tspUrl = url.replaceAll("api", "");
        User user = userService._get(uid);
        if (Objects.isNull(user)) ErrorCodeEnum.USER_NOT_EXIST.throwException();
        String referral_code = user.getReferral_code();
        String referral_url = tspUrl + "#/ground?referral_code=" + referral_code;
        return referral_url;
    }

    /**
     * 直邀普通会员返佣比例(普通场/稳赚场)
     */
    public void inviteRebate(Double normalRate, Boolean ifNormal){
        Long uid = requestInitService.uid();
        Agent agent = agentService.getById(uid);
        if (Objects.isNull(agent)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Double rebateRate = DoubleDecimalTrans.double_divide_hundred(normalRate);
        Double rate;
        if (ifNormal){
            rate = agent.getNormal_rebate_proportion();
            if (rebateRate >= rate) ErrorCodeEnum.throwException("返佣比例应小于" + rate);
            boolean update = agentService.update(new LambdaUpdateWrapper<Agent>()
                    .eq(Agent::getId, uid)
                    .set(Agent::getInvite_normal_rebate_proportion, rebateRate));
            if (!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }else {
            rate = agent.getSteady_rebate_proportion();
            if (rate == 0.0) rate = agent.getNormal_rebate_proportion();
            if (rebateRate >= rate) ErrorCodeEnum.throwException("返佣比例应小于" + rate);
            boolean update = agentService.update(new LambdaUpdateWrapper<Agent>()
                    .eq(Agent::getId, uid)
                    .set(Agent::getInvite_steady_rebate_proportion, rebateRate));
            if (!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
    }


    @Resource
    private RedisLock redisLock;


}
