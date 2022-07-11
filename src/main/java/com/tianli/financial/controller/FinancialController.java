package com.tianli.financial.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.service.AccountSummaryService;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.Constants;
import com.tianli.common.blockchain.CurrencyCoinEnum;
import com.tianli.currency.CurrencyTokenService;
import com.tianli.currency.mapper.CurrencyToken;
import com.tianli.sso.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.account.enums.ProductType;
import com.tianli.account.service.AccountBalanceOperationLogService;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.currency.controller.LogPageDTO;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.Result;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.FinancialLogStatus;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.enums.FinancialProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialLogService;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.impl.FinancialServiceImpl;
import com.tianli.sso.RequestInitService;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequestMapping("/financial")
public class FinancialController {

    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialServiceImpl financialService;
    @Resource
    private FinancialLogService userFinancialLogService;
    @Resource
    private AccountBalanceOperationLogService currencyLogService;
    @Resource
    private AccountBalanceService accountSummaryService;

    @GetMapping("/product/list")
    public Result productList(){
        List<FinancialProduct> list = financialProductService.list(new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, FinancialProductStatus.enable)
                .orderByAsc(FinancialProduct::getPeriod)
        );
        return Result.instance().setData(list);
    }

    @GetMapping("/product/{productId}")
    public Result oneProduct(@PathVariable("productId") Long productId){
        FinancialProduct financialProduct = financialProductService.getById(productId);
        return Result.instance().setData(financialProduct);
    }

    @GetMapping("/my/financial")
    public Result my( @RequestParam(value = "page", defaultValue = "1") Integer page,
                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long uid = requestInitService.uid();
        AccountBalance accountBalance = accountSummaryService.getAndInit(uid, ProductType.financial);
        LocalDate now = requestInitService.now().toLocalDate();
        List<UserFinancialPage> logList = userFinancialLogService.getUserFinancialPage(uid);
        List<UserFinancialPage> createdLogList = logList.stream().filter(o -> FinancialLogStatus.created.name().equals(o.getStatus())).collect(Collectors.toList());
        for(UserFinancialPage userFinancialPage: createdLogList) {
            double profit = 0.0;
            if(FinancialProductType.current.name().equals(userFinancialPage.getType())){
                long period = Math.max(0,userFinancialPage.getStart_date().until(now, DAYS));
                profit = CurrencyAdaptType.usdt_omni.money(new BigDecimal(userFinancialPage.getAmount()).multiply(BigDecimal.valueOf(userFinancialPage.getRate() * period)).toBigInteger());
            }
            if(FinancialProductType.fixed.name().equals(userFinancialPage.getType())){
                long period = userFinancialPage.getStart_date().until(userFinancialPage.getEnd_date(), DAYS);
                long now_period = Math.max(userFinancialPage.getStart_date().until(requestInitService.now().toLocalDate(), DAYS),0);
                profit = CurrencyAdaptType.usdt_omni.money(new BigDecimal(userFinancialPage.getAmount()).multiply(BigDecimal.valueOf(userFinancialPage.getRate() * Math.min(period, now_period))).toBigInteger());
            }
            userFinancialPage.setProfit(profit);
            userFinancialPage.setMoney(CurrencyAdaptType.usdt_omni.money(userFinancialPage.getAmount()));
        }

        double got_profit1 = currencyLogService.list(new LambdaQueryWrapper<AccountBalanceOperationLog>()
                .eq(AccountBalanceOperationLog::getProductType, ProductType.financial.name())
                .eq(AccountBalanceOperationLog::getDes, CurrencyLogDes.赎回前扣除.name())
                .eq(AccountBalanceOperationLog::getUid, uid)
        ).stream().mapToDouble(o-> CurrencyAdaptType.usdt_omni.money(o.getAmount())).sum();
        double got_profit2 = currencyLogService.list(new LambdaQueryWrapper<AccountBalanceOperationLog>()
                .eq(AccountBalanceOperationLog::getProductType, ProductType.financial.name())
                .eq(AccountBalanceOperationLog::getDes, CurrencyLogDes.赎回.name())
                .eq(AccountBalanceOperationLog::getUid, uid)
        ).stream().mapToDouble(o-> CurrencyAdaptType.usdt_omni.money(o.getAmount())).sum();
        double got_profit = Math.max(got_profit2 - got_profit1, 0.0);
        double to_get_profit = createdLogList.stream().mapToDouble(UserFinancialPage::getProfit).sum();

        return Result.instance().setData(
                MapTool.Map().put("balance", CurrencyAdaptType.usdt_omni.money(accountBalance.getBalance()))
                .put("remain", CurrencyAdaptType.usdt_omni.money(accountBalance.getRemain()))
                .put("freeze", CurrencyAdaptType.usdt_omni.money(accountBalance.getFreeze()))
                .put("list", createdLogList.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList()))
                .put("all_profit", got_profit + to_get_profit)
                .put("count",createdLogList.size())
        );
    }

    @GetMapping("/my/bill")
    public Result myBill(LogPageDTO dto) {
        Long uid = requestInitService.uid();
        Wrapper<AccountBalanceOperationLog> queryWrapper = new LambdaQueryWrapper<AccountBalanceOperationLog>()
                .eq(AccountBalanceOperationLog::getUid, uid)
                .in(AccountBalanceOperationLog::getLogType, Lists.newArrayList(AccountOperationType.reduce, AccountOperationType.increase, AccountOperationType.freeze, AccountOperationType.unfreeze))
                .eq(AccountBalanceOperationLog::getProductType, ProductType.financial)
                .ne(AccountBalanceOperationLog::getDes, CurrencyLogDes.赎回前扣除.name())
                .eq(Objects.nonNull(dto.getDes()), AccountBalanceOperationLog::getDes, dto.getDes())
                .orderByDesc(AccountBalanceOperationLog::getId);
        Page<AccountBalanceOperationLog> page = currencyLogService.page(new Page<>(dto.getPage(), dto.getSize()), queryWrapper);
        long count = currencyLogService.count(queryWrapper);
        List<LogPageVO> voList = page.getRecords().stream().map(LogPageVO::trans).collect(Collectors.toList());
        return Result.instance().setList(voList, count);
    }

    /**
     * 申购理财产品
     */
    @PostMapping("/purchase")
    public Result purchase(@RequestBody @Valid PurchaseQuery purchaseQuery){
        //TODO币种的转换
        financialService.purchase(purchaseQuery);
        return Result.instance();
    }

}
