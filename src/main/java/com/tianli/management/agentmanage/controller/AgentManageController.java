package com.tianli.management.agentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.bet.BetService;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.common.DoubleDecimalTrans;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.mapper.Currency;
import com.tianli.deposit.ChargeDepositService;
import com.tianli.deposit.mapper.ChargeDeposit;
import com.tianli.deposit.mapper.ChargeDepositStatus;
import com.tianli.deposit.mapper.ChargeDepositType;
import com.tianli.deposit.mapper.DepositSettlementType;
import com.tianli.dividends.DividendsService;
import com.tianli.dividends.mapper.Dividends;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.agentmanage.AgentManageService;
import com.tianli.management.agentmanage.mapper.AgentManagePageDTO;
import com.tianli.rebate.RebateService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserIdentity;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adminManage")
public class AgentManageController {
    @Resource
    private AgentService agentService;

    @Resource
    private BetService betService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private AgentManageService agentManageService;

    @Resource
    private ChargeDepositService chargeDepositService;

    @Resource
    private ChargeSettlementService chargeSettlementService;

    @Resource
    private UserService userService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private CaptchaPhoneService captchaPhoneService;

    @Resource
    private CaptchaEmailService captchaEmailService;

    @Resource
    private RebateService rebateService;

    @Resource
    private DividendsService dividendsService;

    @Resource
    private DoubleDecimalTrans doubleDecimalTrans;

    @Resource
    private UserReferralService userReferralService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.代理商管理)
    public Result page(String nick, String phone, String startTime, String endTime, Boolean focus,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size){

        // 获取分页数据
        int count = agentService.count(new LambdaQueryWrapper<Agent>()
                .eq(Agent::getIdentity, UserIdentity.senior_agent)
                .like(StringUtils.isNotBlank(nick), Agent::getNick, nick)
                .like(StringUtils.isNotBlank(phone), Agent::getUsername, phone)
                .ge(StringUtils.isNotBlank(startTime), Agent::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Agent::getCreate_time, endTime)
                .eq(Objects.nonNull(focus), Agent::getFocus, focus)
        );
        if(count <= 0){
            return Result.success(MapTool.Map()
                    .put("count", 0)
                    .put("list", Lists.newArrayList())
                    .put("stat", MapTool.Map().put("totalDeposit", 0).put("totalNotSettlement", 0))
            );
        }
        // 获取总的保证金
//        double allDepositMoney = agentManageService.getSumDeposit(nick, phone, startTime, endTime, focus);
        // 获取总的未结算的数额
        Map<String, BigDecimal> allSettlement = agentManageService.getSumUnSettlement(nick, phone, startTime, endTime, focus);
        List<AgentManagePageDTO> list = agentManageService.getPage(nick, phone, startTime, endTime,  page, size);
        List<AgentManagePageVO> vos = list.stream().map(AgentManagePageVO::trans).collect(Collectors.toList());
        return Result.success(MapTool.Map()
                .put("count", count)
                .put("list", vos)
                .put("stat", MapTool.Map()
                        .put("totalNotSettlementBF", TokenCurrencyType.BF_bep20.money(allSettlement.get("sumBalanceBF").toBigInteger()))
                        .put("totalNotSettlement", TokenCurrencyType.usdt_omni.money(allSettlement.get("sumBalance").toBigInteger()))
                )
        );
    }

    @GetMapping("agent/rake/detail")
    @AdminPrivilege(and = Privilege.代理商管理)
    public Result rakeDetail(String phone, String startTime, String endTime,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size){
        long count = agentService.rakeDetailCount(phone, startTime, endTime);
        List<AgentRakeDetailDTO> dto = agentService.rakeDetail(phone, startTime, endTime, page, size);
        List<AgentRakeDetailVO> vos = dto.stream().map(AgentRakeDetailVO::trans).collect(Collectors.toList());
        return Result.success(MapTool.Map().put("totalNums",count).put("page",vos));
    }

    @GetMapping("agent/realDepositAmount/{uid}")
    @AdminPrivilege(and = Privilege.代理商管理)
    public Result realDepositAmount(@PathVariable("uid") Long uid){
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        if(Objects.isNull(currency)){
            return Result.instance().setData(0);
        }
        return Result.instance().setData(TokenCurrencyType.usdt_omni.money(currency.getRemain()));
    }

    @PostMapping("/saveAgent")
    @AdminPrivilege(and = Privilege.代理商管理)
    public Result saveAgent(@RequestBody @Valid SaveAgentDTO dto){
        String username = dto.getUsername();
//        captchaPhoneService.verify(username, CaptchaPhoneType.registrationAgent, dto.getCode());
//        captchaEmailService.verify(username, CaptchaPhoneType.registrationAgent, dto.getCode());
        User user = userService._getByUsername(username);
        if(Objects.isNull(user)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Agent dbAgent = agentService.getById(user.getId());
        if(Objects.nonNull(dbAgent)){
            ErrorCodeEnum.REPEAT_SET_AGENT.throwException();
        }
        //设置代理商 不能有邀请人
        UserReferral byId = userReferralService.getById(user.getId());
        if(byId != null)
            ErrorCodeEnum.throwException("该用户存在邀请人，不能设置为代理商");
        //设置为代理商时,必须邀请链往上不能有代理商
//        LinkedList<Long> uids = userReferralService.userReferralChain(user.getId());
//        uids.forEach(uid->{
//            Agent byId = agentService.getById(uid);
//            if(Objects.nonNull(byId)){
//                ErrorCodeEnum.FORBID_SET_AGENT.throwException();
//            }
//        });
        // 计算实际的分红比例
//        double expect_deposit = dto.getExpect_deposit();
        double expect_dividends = dto.getExpect_dividends();
        //        Currency currency = currencyService._get(user.getId(), CurrencyTypeEnum.deposit);
//        if(currency != null){
//            double money = TokenCurrencyType.usdt_omni.money(currency.getRemain());
//            real_dividends = Math.min((money / expect_deposit) * expect_dividends, expect_dividends);
//        }

        // 创建agent对象
        Agent agent = Agent.builder()
                .id(user.getId())
                .create_time(LocalDateTime.now())
                .identity(UserIdentity.senior_agent)
                .senior_id(0L)
                .nick(dto.getNick())
                .username(username)
//                .expect_deposit(TokenCurrencyType.usdt_omni.amount(expect_deposit))
                .profit(BigInteger.ZERO)
                .settled_number(BigInteger.ZERO)
                .expect_dividends(doubleDecimalTrans.double_divide_hundred(expect_dividends))
                .real_dividends(doubleDecimalTrans.double_divide_hundred(expect_dividends))
                .steady_dividends(doubleDecimalTrans.double_divide_hundred(dto.getSteady_dividends()))
//                .normal_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getNormal_rebate_proportion()))
//                .steady_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getSteady_rebate_proportion()))
                .focus(false)
                .note(dto.getNote())
                .build();
        agentManageService.saveAgent(agent);
        return Result.success();
    }

    @PostMapping("/updateAgent")
    @AdminPrivilege(and = Privilege.代理商管理)
    public Result updateAgent(@RequestBody @Valid UpdateAgentDTO dto){
        String username = dto.getUsername();
//        captchaPhoneService.verify(username, CaptchaPhoneType.registrationAgent, dto.getCode());
//        captchaEmailService.verify(username, CaptchaPhoneType.registrationAgent, dto.getCode());
        User user = userService._getByUsername(username);
        if(Objects.isNull(user)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(!Objects.equals(user.getId(), dto.getId())){
            ErrorCodeEnum.USER_PHONE_USED_REPEAT.throwException();
        }
        Agent dbAgent = agentService.getById(user.getId());
        if(Objects.isNull(dbAgent)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(dbAgent.getIdentity() != UserIdentity.senior_agent){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        // 计算实际的分红比例
//        Currency currency = currencyService.get(user.getId(), CurrencyTypeEnum.deposit);
//        double money = TokenCurrencyType.usdt_omni.money(currency.getRemain());
        double expect_dividends = dto.getExpect_dividends();
//        double expect_deposit = dto.getExpect_deposit();
//        double real_dividends = Math.min((money / expect_deposit) * expect_dividends, expect_dividends);
        // 创建agent对象
        Agent agent = Agent.builder()
                .id(user.getId())
                .nick(dto.getNick())
//                .expect_deposit(TokenCurrencyType.usdt_omni.amount(expect_deposit))
                .expect_dividends(doubleDecimalTrans.double_divide_hundred(expect_dividends))
                .real_dividends(doubleDecimalTrans.double_divide_hundred(expect_dividends))
                .steady_dividends(doubleDecimalTrans.double_divide_hundred(dto.getSteady_dividends()))
//                .normal_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getNormal_rebate_proportion()))
//                .steady_rebate_proportion(doubleDecimalTrans.double_divide_hundred(dto.getSteady_rebate_proportion()))
                .note(dto.getNote())
                .build();
        if(!agentService.updateById(agent)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return Result.success();
    }

    @PostMapping("/deleteAgent/{id}")
    @AdminPrivilege(and = Privilege.代理商管理)
    public Result deleteAgent(@PathVariable("id") Long id){
        Agent byId = agentService.getById(id);
        if(Objects.isNull(byId)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Currency settlementCurrency = currencyService.get(id, CurrencyTypeEnum.settlement);
        Currency depositCurrency = currencyService.get(id, CurrencyTypeEnum.deposit);
        if(!((settlementCurrency == null ||settlementCurrency.getBalance() == null || BigInteger.ZERO.equals(settlementCurrency.getBalance()))
                && (depositCurrency == null ||depositCurrency.getBalance() == null || BigInteger.ZERO.equals(depositCurrency.getBalance())))){
            ErrorCodeEnum.throwException("删除失败，入驻金额或未结算金额不为0");
        }
        int count = agentService.count(new LambdaQueryWrapper<Agent>().eq(Agent::getSenior_id, id));
        if(count > 0){
            ErrorCodeEnum.EXIST_LOW_AGENT.throwException();
        }
        agentManageService.deleteAgent(id);
        return Result.success();
    }

    public static void main(String[] args) {
        ArrayList<Integer> integers = new ArrayList<>();
        while (true){
            integers.add(1);
        }
    }

    @GetMapping("/team/bet/page/{uid}")
    @AdminPrivilege(and = {Privilege.代理商管理})
    public Result teamBetPage(String phone, BetResultEnum result, String startTime, String endTime,
                              @PathVariable("uid") Long uid,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){
        List<Dividends> list = dividendsService.list(Wrappers.lambdaQuery(Dividends.class).eq(Dividends::getDividends_uid, uid));
//        List<Rebate> list = rebateService.list(new LambdaQueryWrapper<Rebate>().eq(Rebate::getRebate_uid, uid));
        if(CollectionUtils.isEmpty(list)){
            return Result.success(MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("start", MapTool.Map()
                            .put("sumBetAmount", 0)
                            .put("sumRebateAmount", 0)
                            .put("sumDividendsAmount", 0)
                    )
            );
        }
        List<Long> betIdList = list.stream().map(Dividends::getBet_id).collect(Collectors.toList());

        //计算三个总和
        Map<String, BigDecimal> sumBet = agentManageService.getSumBet(betIdList, phone, result, startTime, endTime);
        // 全部的押注金额
        BigInteger sumBetAmount = sumBet.get("sumBetAmount").toBigInteger();
        // 代理商分红净盈亏总额
//        BigInteger sumDividendsAmount = sumBet.get("sumDividendsAmount").toBigInteger();
        // 代理商平台返佣 USDT
        BigInteger sumRebateAmount = sumBet.get("sumRebateAmount").toBigInteger();
        // 代理商平台返佣 BF
        BigInteger sumRebateAmountBF = sumBet.get("sumRebateAmountBF").toBigInteger();
        LambdaQueryWrapper<Bet> queryWrapper = new LambdaQueryWrapper<Bet>()
                .ge(StringUtils.isNotBlank(startTime), Bet::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Bet::getCreate_time, endTime)
                .like(StringUtils.isNotBlank(phone), Bet::getUid_username, phone)
                .eq(Objects.nonNull(result), Bet::getResult, result)
                .in(Bet::getId, betIdList)
                .orderByDesc(Bet::getId);
        Page<Bet> betPage = betService.page(new Page<>(page, size), queryWrapper);
        // 分页数据
        List<Bet> records = betPage.getRecords();
        List<TeamBetVO> vos = records.stream().map(TeamBetVO::trans).collect(Collectors.toList());
        Map<Long, Dividends> dividendsMap = list.stream().collect(Collectors.toMap(Dividends::getBet_id, Function.identity()));
        vos.forEach(e -> {
            Dividends dividends = dividendsMap.get(e.getId());
            if(Objects.nonNull(dividends)){
                BigInteger my_profit = dividends.getMy_profit();
                CurrencyTokenEnum profit_token = dividends.getProfit_token();
                e.setToken(profit_token);
                if(Objects.equals(profit_token, CurrencyTokenEnum.usdt_omni)){
                    e.setPf_profit(TokenCurrencyType.usdt_omni.money(my_profit));
                }else{
                    e.setPf_profit(CurrencyTokenEnum.BF_bep20.money(my_profit));
                }
            }
        });
        return Result.success(MapTool.Map()
                .put("total", betPage.getTotal())
                .put("list", vos)
                .put("start", MapTool.Map()
                        .put("sumBetAmount", TokenCurrencyType.usdt_omni.money(sumBetAmount))
                        .put("sumRebateAmount", TokenCurrencyType.usdt_omni.money(sumRebateAmount))
                        .put("sumRebateAmountBF", CurrencyTokenEnum.BF_bep20.money(sumRebateAmountBF))
                )
        );
    }

    @GetMapping("/balance/account/page/{uid}")
    @AdminPrivilege(and = {Privilege.代理商管理})
    public Result balanceAccountPage(String startTime, String endTime,
                              @PathVariable("uid") Long uid,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){
        Page<ChargeDeposit> depositPage = chargeDepositService.page(new Page<>(page, size), new LambdaQueryWrapper<ChargeDeposit>()
                .eq(ChargeDeposit::getUid, uid)
                .ge(StringUtils.isNotBlank(startTime), ChargeDeposit::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), ChargeDeposit::getCreate_time, endTime)
                .eq(ChargeDeposit::getSettlement_type, DepositSettlementType.balance)
                .orderByDesc(ChargeDeposit::getId));
        long total;
        if((total = depositPage.getTotal()) <= 0){
            return Result.success(MapTool.Map().put("total", 0).put("list", Lists.newArrayList()));
        }
        List<ChargeDeposit> records = depositPage.getRecords();
        List<ChargeDepositVO> vos = records.stream().map(ChargeDepositVO::trans).collect(Collectors.toList());
        return Result.success(MapTool.Map().put("total", total).put("list", vos));
    }

    @PostMapping("/save/balance/account/{uid}")
    @AdminPrivilege(and = {Privilege.代理商管理})
    public Result saveBalanceAccount(@PathVariable("uid") Long uid,
                                     @RequestBody @Valid SaveBalanceAccountDTO dto){
        User user = userService._get(uid);
        if(Objects.isNull(user)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        Agent agent = agentService.getById(uid);
        if(Objects.isNull(agent)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        if(Objects.isNull(currency)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        if(Objects.isNull(userInfo)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        BigInteger amount;
        if(currency.getRemain().compareTo(amount = TokenCurrencyType.usdt_omni.amount(dto.getAmount())) < 0){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        agentManageService.saveBalanceAccount(uid, dto, amount, agent.getNick(), agent.getUsername());
        return Result.success();
    }

    @PostMapping("/update/balance/account/{uid}")
    @AdminPrivilege(and = {Privilege.代理商管理})
    public Result updateBalanceAccount(@PathVariable("uid") Long uid,
                                       @RequestBody @Valid UpdateBalanceAccountDTO dto){
        agentManageService.updateBalanceAccount(uid, dto);
        return Result.success();
    }

    @DeleteMapping("/del/ba/{id}/aid/{uid}")
    @AdminPrivilege(and = {Privilege.代理商管理})
    public Result delBalanceAccount(@PathVariable("uid") Long uid,
                                    @PathVariable("id") Long id){
        agentManageService.delBalanceAccount(uid, id);
        return Result.success();
    }

    @GetMapping("/deposit/currency/{uid}")
    @AdminPrivilege(and = {Privilege.代理商保证金流水记录, Privilege.代理商管理})
    public Result getDeposit(@PathVariable("uid") Long uid){
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        if(Objects.isNull(currency)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        return Result.success(MapTool.Map()
                .put("balance", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("remain", TokenCurrencyType.usdt_omni.money(currency.getRemain())));
    }

    @GetMapping("/deposit/recharge/page")
    @AdminPrivilege(and = {Privilege.代理商保证金流水记录})
    public Result depositRechargePage(String nike, String phone,
                              String txid, String startTime, String endTime,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){
        Page<ChargeDeposit> depositPage = chargeDepositService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ChargeDeposit>()
                        .eq(ChargeDeposit::getCharge_type, ChargeDepositType.recharge)
                        .like(StringUtils.isNotBlank(nike), ChargeDeposit::getUid_nick, nike)
                        .like(StringUtils.isNotBlank(phone), ChargeDeposit::getUid_username, phone)
                        .like(StringUtils.isNotBlank(txid), ChargeDeposit::getTxid, txid)
                        .ge(StringUtils.isNotBlank(startTime), ChargeDeposit::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ChargeDeposit::getCreate_time, endTime)
                        .orderByDesc(ChargeDeposit::getCreate_time)
        );
        long total = depositPage.getTotal();
        if(total <= 0){
            return Result.success(MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("sumAmount", 0)
            );
        }
        BigInteger sumAmountErc20 = chargeDepositService.getSumAmount(nike, phone, txid,
                ChargeDepositStatus.transaction_success, DepositSettlementType.chain,
                startTime, endTime, ChargeDepositType.recharge, TokenCurrencyType.usdt_erc20).get("allAmount").toBigInteger();
        BigInteger sumAmountOmni = chargeDepositService.getSumAmount(nike, phone, txid,
                ChargeDepositStatus.transaction_success, DepositSettlementType.chain,
                startTime, endTime, ChargeDepositType.recharge, TokenCurrencyType.usdt_omni).get("allAmount").toBigInteger();
        BigInteger sumAmountErc20Omni = sumAmountErc20.multiply(new BigInteger("100"));
        double sumAmount = TokenCurrencyType.usdt_omni.money(sumAmountErc20Omni.add(sumAmountOmni));
        List<ChargeDeposit> records = depositPage.getRecords();
        List<DepositRechargeVO> vos = records.stream().map(DepositRechargeVO::trans).collect(Collectors.toList());
        return Result.success(MapTool.Map()
                .put("total", total)
                .put("list", vos)
                .put("sumAmount", sumAmount)
        );
    }

    @GetMapping("/deposit/withdraw/page")
    @AdminPrivilege(and = {Privilege.代理商保证金流水记录})
    public Result depositWithdrawPage(String nike, String phone,
                              ChargeDepositStatus status, DepositSettlementType settlement_type,
                              String startTime, String endTime,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){
        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
            Map<String, BigDecimal> sumAmountErc20 = chargeDepositService.getSumAmount(nike, phone, status, settlement_type, startTime, endTime, ChargeDepositType.withdraw, TokenCurrencyType.usdt_erc20);
            BigInteger executingAmountErc20 = sumAmountErc20.get("executingAmount").toBigInteger();
            BigInteger sucAmountErc20 = sumAmountErc20.get("sucAmount").toBigInteger();
            Map<String, BigDecimal> sumAmountOmni = chargeDepositService.getSumAmount(nike, phone, status, settlement_type, startTime, endTime, ChargeDepositType.withdraw, TokenCurrencyType.usdt_omni);
            BigInteger executingAmountOmni = sumAmountOmni.get("executingAmount").toBigInteger();
            BigInteger sucAmountOmni = sumAmountOmni.get("sucAmount").toBigInteger();

            Pair<DigitalCurrency, Double> executingAmountOmni_ = new DigitalCurrency(TokenCurrencyType.usdt_erc20, executingAmountErc20).toOtherAndPrice(TokenCurrencyType.usdt_omni);
            double sumExecutingAmountErc20 = TokenCurrencyType.usdt_omni.money(executingAmountOmni_.getValue0().getAmount().add(executingAmountOmni));
            Pair<DigitalCurrency, Double> sucAmountOmni_ = new DigitalCurrency(TokenCurrencyType.usdt_erc20, sucAmountErc20).toOtherAndPrice(TokenCurrencyType.usdt_omni);
            double sumSucAmountErc20 = TokenCurrencyType.usdt_omni.money(sucAmountOmni_.getValue0().getAmount().add(sucAmountOmni));
            return MapTool.Map().put("sumAmount", sumSucAmountErc20).put("excAmount", sumExecutingAmountErc20);
        });
        Page<ChargeDeposit> depositPage = chargeDepositService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ChargeDeposit>()
                        .eq(ChargeDeposit::getCharge_type, ChargeDepositType.withdraw)
                        .like(StringUtils.isNotBlank(nike), ChargeDeposit::getUid_nick, nike)
                        .like(StringUtils.isNotBlank(phone), ChargeDeposit::getUid_username, phone)
                        .eq(Objects.nonNull(status), ChargeDeposit::getStatus, status)
                        .eq(Objects.nonNull(settlement_type), ChargeDeposit::getSettlement_type, settlement_type)
                        .ge(StringUtils.isNotBlank(startTime), ChargeDeposit::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ChargeDeposit::getCreate_time, endTime)
                        .orderByDesc(ChargeDeposit::getCreate_time)
        );
        long total = depositPage.getTotal();
        if(total <= 0){
            return Result.success(MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("stat", MapTool.Map())
            );
        }

        List<ChargeDeposit> records = depositPage.getRecords();
        List<DepositWithdrawVO> vos = records.stream().map(DepositWithdrawVO::trans).collect(Collectors.toList());
        Map<String, Object> map;
        try {
            map = future.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
            map = Maps.newHashMap();
        }
        return Result.success(MapTool.Map()
                .put("total", total)
                .put("list", vos)
                .put("stat", map)
        );
    }

    @GetMapping("/currency/deposit/{sn}")
    @AdminPrivilege(and = Privilege.代理商保证金流水记录)
    public Result currencyDeposit(@PathVariable("sn") String sn){
        CurrencyLog currencyLog = currencyLogService.getOne(new LambdaQueryWrapper<CurrencyLog>().eq(CurrencyLog::getSn, sn).eq(CurrencyLog::getDes, "撤回"));
        if(Objects.isNull(currencyLog)){
            return Result.instance().setData(0);
        }
        return Result.instance()
                .setData(TokenCurrencyType.usdt_omni.money(currencyLog.getRemain().add(currencyLog.getAmount())));
    }

    @PostMapping("deposit/withdraw/audit")
    @AdminPrivilege(and = Privilege.代理商保证金流水记录)
    public Result depositAudit(@RequestBody DepositAuditDTO dto){
        agentManageService.depositAudit(dto);
        return Result.success();
    }


    @GetMapping("/settlement/recharge/page")
    @AdminPrivilege(and = Privilege.代理商结算明细)
    public Result settlementRechargePage(String phone,
                              String txid, String startTime, String endTime, DepositSettlementType type,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){
        Page<ChargeSettlement> depositPage = chargeSettlementService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ChargeSettlement>()
                        .eq(ChargeSettlement::getCharge_type, ChargeSettlementType.recharge)
                        .eq(Objects.nonNull(type),ChargeSettlement::getSettlement_type,type)
                        .like(StringUtils.isNotBlank(phone), ChargeSettlement::getUid_username, phone)
                        .like(StringUtils.isNotBlank(txid), ChargeSettlement::getTxid, txid)
                        .ge(StringUtils.isNotBlank(startTime), ChargeSettlement::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ChargeSettlement::getCreate_time, endTime)
                        .orderByDesc(ChargeSettlement::getCreate_time)
        );
        long total = depositPage.getTotal();
        if(total <= 0){
            return Result.success(MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
            );
        }
        return Result.success(MapTool.Map()
                .put("total", total)
                .put("list", depositPage.getRecords().stream().map(ChargeSettlementVO::trans).collect(Collectors.toList()))
        );
    }

    @GetMapping("/settlement/withdraw/page")
    @AdminPrivilege(and = Privilege.代理商结算明细)
    public Result settlementWithdrawPage(String txid, ChargeDepositStatus status,
                              String startTime, String endTime,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size){

        Page<ChargeSettlement> depositPage = chargeSettlementService.page(new Page<>(page, size),
                new LambdaQueryWrapper<ChargeSettlement>()
                        .eq(ChargeSettlement::getCharge_type, ChargeSettlementType.withdraw)
                        .like(StringUtils.isNotBlank(txid), ChargeSettlement::getTxid, txid)
                        .eq(Objects.nonNull(status), ChargeSettlement::getStatus, status)
                        .ge(StringUtils.isNotBlank(startTime), ChargeSettlement::getCreate_time, startTime)
                        .le(StringUtils.isNotBlank(endTime), ChargeSettlement::getCreate_time, endTime)
                        .orderByDesc(ChargeSettlement::getCreate_time)
        );
        long total = depositPage.getTotal();
        if(total <= 0){
            return Result.success(MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
            );
        }

        return Result.success(MapTool.Map()
                .put("total", total)
                .put("list", depositPage.getRecords().stream().map(ChargeSettlementVO::trans).collect(Collectors.toList()))
        );
    }

    @GetMapping("/currency/settlement/{sn}")
    @AdminPrivilege(and = Privilege.代理商结算明细)
    public Result currencySettlement(@PathVariable("sn") String sn){
        CurrencyLog currencyLog = currencyLogService.getOne(new LambdaQueryWrapper<CurrencyLog>().eq(CurrencyLog::getSn, sn));
        if(Objects.isNull(currencyLog)){
            return Result.instance().setData(0);
        }
        return Result.instance()
                .setData(TokenCurrencyType.usdt_omni.money(currencyLog.getRemain().add(currencyLog.getAmount())));
    }

    @PostMapping("settlement/withdraw/audit")
    @AdminPrivilege(and = Privilege.代理商结算明细)
    public Result settlementAudit(@RequestBody SettlementAuditDTO dto){
        agentManageService.settlementAudit2(dto);
        return Result.success();
    }
}
