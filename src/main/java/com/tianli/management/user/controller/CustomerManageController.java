package com.tianli.management.user.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.async.AsyncService;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.dto.CurrencyTokenPage;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.CurrencyToken;
import com.tianli.currency_token.order.TokenDealService;
import com.tianli.currency_token.token.TokenListService;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.loan.entity.LoanCurrency;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.management.salesman.entity.Salesman;
import com.tianli.management.salesman.entity.SalesmanUser;
import com.tianli.management.salesman.enums.SalesmanEnum;
import com.tianli.management.salesman.service.SalesmanService;
import com.tianli.management.salesman.service.SalesmanUserService;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.management.user.CustomerManageService;
import com.tianli.management.user.mapper.CustomerDTO;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.mapper.UserStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer")
public class CustomerManageController {

    @Resource
    private CustomerManageService customerManageService;
    @Resource
    private CurrencyService currencyService;

    @Resource
    SalesmanUserService salesmanUserService;

    @Resource
    SalesmanService salesmanService;

    @Resource
    AdminService adminService;

    @Resource
    ChargeService chargeService;

    @Resource
    private CurrencyTokenService currencyTokenService;

    @Resource
    private TokenListService tokenListService;

    @Resource
    SGChargeService sgChargeService;

    @Resource
    private TokenDealService tokenDealService;

    @Resource
    private AsyncService asyncService;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result page(String phone, Integer user_type, UserStatus status, String startTime, String endTime, Long salesman_id,
                       String operationStartTime, String operationEndTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) throws ExecutionException, InterruptedException {
        AdminAndRoles my = adminService.my();
        String roleName = my.getRole().getName();
        Set<Long> queryUser = getQueryUser(salesman_id, my);
        if ((SalesmanEnum.isSalesman(roleName) && CollUtil.isEmpty(queryUser))
                || (!SalesmanEnum.isSalesman(roleName)) && ObjectUtil.isNotNull(salesman_id) && CollUtil.isEmpty(queryUser)) {
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        String queryUserIds = null;
        if (CollUtil.isNotEmpty(queryUser)) {
            queryUserIds = ArrayUtil.join(queryUser.toArray(), ",");
        }
        int count = customerManageService.count(phone, status, user_type, startTime, endTime, queryUserIds);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        List<CustomerDTO> list = customerManageService.page(phone, status, user_type, startTime, endTime, page, size, queryUserIds);
        List<Long> userIds = list.stream().map(CustomerDTO::getId).collect(Collectors.toList());
        //设置小组
        var setSalesmanUsernameFuture = asyncService.async(() -> setSalesmanUsername(list));
        //设置利润明细
        var setProfitFuture = asyncService.async(() -> setProfit(operationStartTime, operationEndTime, userIds, list));
        BigInteger sumBalance = customerManageService.sumBalance(phone, status, user_type, startTime, endTime, queryUserIds);
        BigInteger sumBalanceBF = customerManageService.sumBalanceBF(phone, status, user_type, startTime, endTime, queryUserIds);
        BigDecimal newSumBalance = customerManageService.newSumBalance(phone, status, user_type, startTime, endTime, queryUserIds);
        setSalesmanUsernameFuture.get();
        setProfitFuture.get();
        List<CustomerVO> vos = list.stream().map(CustomerVO::trans).collect(Collectors.toList());
        //期货u余额+现货u余额
        setSgUsdBalance(userIds, vos);
        return Result.instance().setData(MapTool.Map().put("total", count).put("list", vos)
                .put("sumBalance", Convert.toBigDecimal(TokenCurrencyType.usdt_omni.money(sumBalance)).add(newSumBalance))
                .put("sumBalanceBF", TokenCurrencyType.BF_bep20.money(sumBalanceBF)));
    }

    private void setSgUsdBalance(List<Long> userIds, List<CustomerVO> vos) {
        //查询现货
        List<CurrencyToken> currencyTokens = currencyTokenService.getUByUserIds(userIds);
        if (CollUtil.isNotEmpty(currencyTokens)) {
            Map<Long, List<CurrencyToken>> currencyTokenMap = currencyTokens.stream().collect(Collectors.groupingBy(CurrencyToken::getUid));
            for (CustomerVO customerVO : vos) {
                List<CurrencyToken> byIdCurrencyToken = currencyTokenMap.get(customerVO.getId());
                if (CollUtil.isNotEmpty(byIdCurrencyToken)) {
                    for (CurrencyToken currencyToken : byIdCurrencyToken) {
                        customerVO.setBalance(Convert.toDouble(Convert.toBigDecimal(customerVO.getBalance()).add(currencyToken.getBalance())));
                    }
                }
            }
        }
        //查询贷款余额
        List<LoanCurrency> loanCurrencyList = loanCurrencyService.findByUids(userIds, CurrencyCoinEnum.usdt);
        if (CollUtil.isNotEmpty(loanCurrencyList)) {
            Map<Long, LoanCurrency> loanCurrencyMap = loanCurrencyList.stream().collect(Collectors.toMap(LoanCurrency::getUid, Function.identity(), (v1, v2) -> v1));
            for (CustomerVO customerVO : vos) {
                LoanCurrency loanCurrency = loanCurrencyMap.get(customerVO.getId());
                if (ObjectUtil.isNotNull(loanCurrency)) {
                    customerVO.setBalance(Convert.toDouble(Convert.toBigDecimal(customerVO.getBalance()).add(loanCurrency.getBalance())));
                }
            }
        }
    }

    private void setProfit(String startTime, String endTime, List<Long> userIds, List<CustomerDTO> list) {
        List<String> des = ListUtil.of(ChargeType.recharge.name(), ChargeType.withdraw.name());
        //老的充值管理
        Map<Long, List<Charge>> chargeMap = getChargeMap(startTime, endTime, userIds, des);
        //新的现货充值提现管理
        Map<Long, List<SGCharge>> sgChargeMap = getSgChargeMap(startTime, endTime, userIds, des);
        for (CustomerDTO customerDTO : list) {
            BigDecimal recharge_amount = BigDecimal.ZERO;
            BigDecimal withdrawal_amount = BigDecimal.ZERO;
            List<Charge> chargeList = chargeMap.get(customerDTO.getId());
            if (CollUtil.isNotEmpty(chargeList)) {
                for (Charge charge : chargeList) {
                    TokenCurrencyType tokenCurrencyType = TokenCurrencyType.getTokenCurrencyType(charge.getToken().name());
                    if (charge.getCharge_type().equals(ChargeType.recharge)) {
                        recharge_amount = recharge_amount.add(Convert.toBigDecimal(tokenCurrencyType.money(charge.getAmount())));
                    } else {
                        withdrawal_amount = withdrawal_amount.add(Convert.toBigDecimal(tokenCurrencyType.money(charge.getAmount())));
                    }
                }
            }
            List<SGCharge> sgChargeList = sgChargeMap.get(customerDTO.getId());
            if (CollUtil.isNotEmpty(sgChargeList)) {
                for (SGCharge sgCharge : sgChargeList) {
                    if (ObjectUtil.equal(ChargeType.recharge, sgCharge.getCharge_type())) {
                        recharge_amount = recharge_amount.add(sgCharge.getAmount());
                    } else {
                        withdrawal_amount = withdrawal_amount.add(sgCharge.getAmount());
                    }
                }
            }
            customerDTO.setRecharge_amount(recharge_amount);
            customerDTO.setWithdrawal_amount(withdrawal_amount);
            customerDTO.setProfit(recharge_amount.subtract(withdrawal_amount));
        }
    }

    private Map<Long, List<SGCharge>> getSgChargeMap(String startTime, String endTime, List<Long> userIds, List<String> des) {
        LambdaQueryWrapper<SGCharge> sgChargeWrapper = Wrappers.lambdaQuery(SGCharge.class)
                .in(SGCharge::getUid, userIds)
                .eq(SGCharge::getStatus, ChargeStatus.chain_success)
                .in(SGCharge::getToken, ListUtil.of(CurrencyCoinEnum.usdt.name(), CurrencyCoinEnum.usdc.name()))
                .in(SGCharge::getCharge_type, des);
        if (StrUtil.isNotBlank(startTime)) {
            sgChargeWrapper.ge(SGCharge::getCreate_time, startTime);
        }
        if (StrUtil.isNotBlank(endTime)) {
            sgChargeWrapper.le(SGCharge::getCreate_time, endTime);
        }
        List<SGCharge> sgCharges = sgChargeService.list(sgChargeWrapper);
        Map<Long, List<SGCharge>> sgChargeMap = new HashMap<>();

        if (CollUtil.isNotEmpty(sgCharges)) {
            sgChargeMap = sgCharges.stream().collect(Collectors.groupingBy(SGCharge::getUid));
        }
        return sgChargeMap;
    }

    private Map<Long, List<Charge>> getChargeMap(String startTime, String endTime, List<Long> userIds, List<String> des) {
        //老的充值提现管理
        LambdaQueryWrapper<Charge> queryWrapper = Wrappers.lambdaQuery(Charge.class)
                .in(Charge::getUid, userIds)
                .eq(Charge::getStatus, ChargeStatus.chain_success)
                .in(Charge::getCharge_type, des);
        if (StrUtil.isNotBlank(startTime)) {
            queryWrapper.ge(Charge::getCreate_time, startTime);
        }
        if (StrUtil.isNotBlank(endTime)) {
            queryWrapper.le(Charge::getCreate_time, endTime);
        }
        List<Charge> currencyList = chargeService.list(queryWrapper);
        Map<Long, List<Charge>> chargeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(currencyList)) {
            chargeMap = currencyList.stream().collect(Collectors.groupingBy(Charge::getUid));
        }
        return chargeMap;
    }


    private Set<Long> getQueryUser(Long salesman_id, AdminAndRoles my) {
        Set<Long> userIds = new HashSet<>();
        Salesman salesman = salesmanService.getOne(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getAdmin_id, my.getId()));
        if (SalesmanEnum.isSalesman(my.getRole().getName())) {
            if (ObjectUtil.isNull(salesman_id) && ObjectUtil.isNull(salesman.getP_id())) {
                //如果是组长查询组长以及下面成员所分配的用户id
                List<Salesman> salesmanList = salesmanService.list(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getP_id, salesman.getId()));
                if (ObjectUtil.isNull(salesmanList)) {
                    salesmanList = new ArrayList<>();
                }
                //加上组长自己
                salesmanList.add(salesman);
                List<Long> salesmanIds = salesmanList.stream().map(Salesman::getId).collect(Collectors.toList());
                List<SalesmanUser> salesmanUsers = salesmanUserService.list(Wrappers.lambdaQuery(SalesmanUser.class).in(SalesmanUser::getSalesman_id, salesmanIds));
                if (CollUtil.isNotEmpty(salesmanUsers)) {
                    salesmanUsers.forEach(salesmanUser -> userIds.add(salesmanUser.getUser_id()));
                }
            } else if (ObjectUtil.isNull(salesman_id) && ObjectUtil.isNotNull(salesman.getP_id())) {
                List<SalesmanUser> salesmanUsers = salesmanUserService.list(Wrappers.lambdaQuery(SalesmanUser.class).eq(SalesmanUser::getSalesman_id, salesman.getId()));
                if (CollUtil.isNotEmpty(salesmanUsers)) {
                    salesmanUsers.forEach(salesmanUser -> userIds.add(salesmanUser.getUser_id()));
                }
            } else {
                Salesman paramSalesman = salesmanService.getById(salesman_id);
                if (ObjectUtil.isNull(paramSalesman)) {
                    throw ErrorCodeEnum.SALESMAN_NOT_FOUND.generalException();
                }
                if (ObjectUtil.notEqual(salesman.getId(), paramSalesman.getId()) && ObjectUtil.notEqual(salesman.getId(), paramSalesman.getP_id())) {
                    throw ErrorCodeEnum.ACCESS_DENY.generalException();
                }
                List<SalesmanUser> salesmanUsers = salesmanUserService.list(Wrappers.lambdaQuery(SalesmanUser.class).eq(SalesmanUser::getSalesman_id, salesman_id));
                if (CollUtil.isNotEmpty(salesmanUsers)) {
                    salesmanUsers.forEach(salesmanUser -> userIds.add(salesmanUser.getUser_id()));
                }
            }
        } else if (ObjectUtil.isNotNull(salesman_id)) {
            List<SalesmanUser> salesmanUsers = salesmanUserService.list(Wrappers.lambdaQuery(SalesmanUser.class).eq(SalesmanUser::getSalesman_id, salesman_id));
            if (CollUtil.isNotEmpty(salesmanUsers)) {
                salesmanUsers.forEach(salesmanUser -> userIds.add(salesmanUser.getUser_id()));
            }
        }
        return userIds;
    }

    private void setSalesmanUsername(List<CustomerDTO> list) {
        if (CollUtil.isNotEmpty(list)) {
            List<Long> userIds = list.stream().map(CustomerDTO::getId).collect(Collectors.toList());
            List<SalesmanUser> salesmanUsers = salesmanUserService.list(Wrappers.lambdaQuery(SalesmanUser.class).in(SalesmanUser::getUser_id, userIds));
            if (CollUtil.isNotEmpty(salesmanUsers)) {
                List<Long> salesmanIds = salesmanUsers.stream().map(SalesmanUser::getSalesman_id).collect(Collectors.toList());
                List<Salesman> salesmanList = salesmanService.listByIds(salesmanIds);
                Map<Long, SalesmanUser> salesmanUserMap = salesmanUsers.stream().collect(Collectors.toMap(SalesmanUser::getUser_id, Function.identity(), (v1, v2) -> v1));
                Map<Long, Salesman> salesmanMap = salesmanList.stream().collect(Collectors.toMap(Salesman::getId, Function.identity()));
                for (CustomerDTO customerDTO : list) {
                    SalesmanUser salesmanUser = salesmanUserMap.get(customerDTO.getId());
                    if (ObjectUtil.isNotNull(salesmanUser)) {
                        Salesman salesman = salesmanMap.get(salesmanUser.getSalesman_id());
                        if (ObjectUtil.isNotNull(salesman)) {
                            customerDTO.setSalesman_username(salesman.getAdmin_username());
                        }
                    }
                }
            }
        }
    }

    @GetMapping("/rebate/page/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result rebatePage(@PathVariable("id") Long id,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        int count = customerManageService.rebateCount(id);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        List<Rebate> list = customerManageService.rebatePage(id, page, size);
        List<CustomerRebateVO> vos = list.stream().map(CustomerRebateVO::trans).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map().put("total", count).put("list", vos));
    }

    @GetMapping("/withdrawal/page/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result withdrawalPage(@PathVariable("id") Long id,
                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size) {
        int count = customerManageService.withdrawalCount(id);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        List<Charge> list = customerManageService.withdrawalPage(id, page, size);
        List<CustomerChargeVO> vos = list.stream().map(CustomerChargeVO::trans).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map().put("total", count).put("list", vos));
    }

    @GetMapping("/referral/page/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result referralPage(@PathVariable("id") Long id,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        int count = customerManageService.referralCount(id);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map().put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        List<CustomerReferralVO> vos = customerManageService.referralPage(id, page, size);
        return Result.instance().setData(MapTool.Map().put("total", count).put("list", vos));
    }

    @GetMapping("/currency/page/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result currencyPage(@PathVariable("id") Long id,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        int count = customerManageService.currencyCount(id);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map()
                    .put("currentBalance", 0).put("availableBalance", 0).put("transitBalance", 0)
                    .put("currentBalanceBF", 0).put("availableBalanceBF", 0).put("transitBalanceBF", 0)
                    .put("total", 0).put("list", Lists.newArrayListWithCapacity(0)));
        }
        Currency currency = currencyService.get(id, CurrencyTypeEnum.normal);
        List<CurrencyLog> list = customerManageService.currencyPage(id, page, size);
        List<CustomerCurrencyVO> vos = list.stream().map(CustomerCurrencyVO::trans).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map().put("total", count).put("list", vos)
                .put("currentBalance", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("availableBalance", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("transitBalance", TokenCurrencyType.usdt_omni.money(currency.getFreeze()))
                .put("currentBalanceBF", TokenCurrencyType.BF_bep20.money(currency.getBalance_BF()))
                .put("availableBalanceBF", TokenCurrencyType.BF_bep20.money(currency.getRemain_BF()))
                .put("transitBalanceBF", TokenCurrencyType.BF_bep20.money(currency.getFreeze_BF())));
    }

    @PostMapping("/status/negate/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result userStatusNegate(@PathVariable("id") Long id) {
        customerManageService.userStatusNegate(id);
        return Result.instance();
    }

    @GetMapping("/robot/id/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result getRobotInfo(@PathVariable Long id) {
        return Result.success(customerManageService.getRobotById(id));
    }

    @PostMapping("/update/robot/id/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result updateRobot(@PathVariable Long id, @RequestBody @Valid CustomerUpdateDTO dto) {
        customerManageService.updateRobot(id, dto);
        return Result.instance();
    }

    @PostMapping("/update/node/id/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result updateNode(@PathVariable("id") Long id, @RequestBody @Valid CustomerUpdateNodeDTO dto) {
        customerManageService.updateNode(id, dto.getNode());
        return Result.instance();
    }

    @PostMapping("/update/creditscore/id/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result updateCreditScore(@PathVariable("id") Long id, @RequestBody @Valid CustomerUpdateCreditScoreDTO dto) {
        customerManageService.updateCreditScore(id, dto.getCredit_score(),dto.getAdjust_reason());
        return Result.instance();
    }

    @PostMapping("/update/user/type/id/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result updateNode(@PathVariable("id") Long id, @RequestBody @Valid CustomerUpdateTypeDTO dto) {
        customerManageService.updateUserType(id, dto.getUser_type());
        return Result.instance();
    }

    @GetMapping("/get/currency/id/{id}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result getCurrencyById(@PathVariable("id") Long id) {
        Currency normalCurrency = customerManageService.getCurrencyById(id, CurrencyTypeEnum.normal);
        BigDecimal normalCurrencyBalance = TokenCurrencyType.usdt_omni._money(normalCurrency.getBalance());
        BigDecimal normalCurrencyRemain = TokenCurrencyType.usdt_omni._money(normalCurrency.getRemain());
        BigDecimal normalCurrencyFreeze = TokenCurrencyType.usdt_omni._money(normalCurrency.getFreeze());
        Currency financialCurrency = customerManageService.getCurrencyById(id, CurrencyTypeEnum.financial);
        BigDecimal financialCurrencyBalance = TokenCurrencyType.usdt_omni._money(financialCurrency.getBalance());
        BigDecimal financialCurrencyRemain = TokenCurrencyType.usdt_omni._money(financialCurrency.getRemain());
        BigDecimal financialCurrencyFreeze = TokenCurrencyType.usdt_omni._money(financialCurrency.getFreeze());
        DiscountCurrency discountCurrency = customerManageService.getDiscountCurrencyById(id);
        BigDecimal discountCurrencyBalance = ObjectUtil.isNull(discountCurrency) ? BigDecimal.ZERO : TokenCurrencyType.usdt_omni._money(discountCurrency.getBalance());
        LoanCurrency loanCurrency = loanCurrencyService.findByUid(id, CurrencyCoinEnum.usdt);
        return Result.success(MapTool.Map()
                .put("normalCurrency", MapTool.Map().put("balance", normalCurrencyBalance).put("freeze", normalCurrencyFreeze).put("remain", normalCurrencyRemain))
                .put("normalCurrency", MapTool.Map().put("balance", normalCurrencyBalance).put("freeze", normalCurrencyFreeze).put("remain", normalCurrencyRemain))
                .put("financialCurrency", MapTool.Map().put("balance", financialCurrencyBalance).put("freeze", financialCurrencyFreeze).put("remain", financialCurrencyRemain))
                .put("discountCurrency", discountCurrencyBalance)
                .put("loanBalance", loanCurrency.getBalance())
                .put("total", normalCurrencyBalance.add(financialCurrencyBalance).add(discountCurrencyBalance).add(loanCurrency.getBalance())));
    }

    @AdminPrivilege(and = Privilege.客户管理)
    @GetMapping("/get/profitDetails/{id}")
    public Result profitDetails(@PathVariable Long id,
                                @RequestParam(required = false) String startTime,
                                @RequestParam(required = false) String endTime) {
        LambdaQueryWrapper<Charge> wrapper = Wrappers.lambdaQuery(Charge.class);
        wrapper.eq(Charge::getUid, id);
        if (StrUtil.isNotBlank(startTime)) {
            wrapper.ge(Charge::getCreate_time, startTime);
        }
        if (StrUtil.isNotBlank(endTime)) {
            wrapper.le(Charge::getCreate_time, endTime);
        }
        List<Charge> chargeList = chargeService.list(wrapper);
        return Result.success(CustomerProfitDetailsVo.get(chargeList));
    }

    /**
     * 现货账户
     *
     * @param uid 用户uid
     * @return
     */
    @GetMapping("/get/spotAccount/{uid}")
    @AdminPrivilege(and = Privilege.客户管理)
    public Result spotAccount(@PathVariable("uid") Long uid) {
        if (Objects.isNull(uid)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        //获取uid下所有虚拟币
        List<CurrencyToken> list = currencyTokenService.list(new LambdaQueryWrapper<CurrencyToken>()
                .gt(CurrencyToken::getBalance, BigDecimal.ZERO).eq(CurrencyToken::getUid, uid));
        Map<CurrencyCoinEnum, TokenList> m = tokenListService.list().stream().collect(Collectors.toMap(TokenList::getToken, Function.identity()));
        List<CurrencyTokenPage> result = list.stream().map(o -> {
            if (o.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                return CurrencyTokenPage.builder()
                        .tokenInfo(m.get(o.getToken()))
                        .token(o.getToken())
                        .balance(o.getBalance().setScale(6, RoundingMode.FLOOR).toString())
                        .remain(o.getRemain().setScale(6, RoundingMode.FLOOR).toString())
                        .freeze(o.getFreeze().setScale(6, RoundingMode.FLOOR).toString()).build();
            }
            return null;
        }).collect(Collectors.toList());


        return Result.instance().setData(MapTool.Map()
                .put("list", result)
                .put("total", result.size())
        );
    }


}
