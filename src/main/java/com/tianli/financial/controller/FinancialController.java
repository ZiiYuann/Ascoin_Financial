package com.tianli.financial.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.common.Constants;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.controller.LogPageDTO;
import com.tianli.currency.controller.LogPageVO;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.CurrencyToken;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.financial.FinancialProductService;
import com.tianli.financial.FinancialService;
import com.tianli.financial.UserFinancialLogService;
import com.tianli.financial.mapper.*;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequestMapping("/financial")
public class FinancialController {

    @GetMapping("/product/list")
    public Result productList(){
        List<FinancialProduct> list = financialProductService.list(new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, FinancialProductStatus.enable)
                .orderByAsc(FinancialProduct::getPeriod)
        );
        return Result.instance().setData(list);
    }

    @GetMapping("/product/{product_id}")
    public Result oneProduct(@PathVariable("product_id") Long product_id){
        FinancialProduct financialProduct = financialProductService.getById(product_id);
        return Result.instance().setData(financialProduct);
    }

//    @GetMapping("/product/list")
//    public Result myProduct(){
//        List<UserFinancialLog> list = userFinancialLogService.list();
//        return Result.instance().setData(list);
//    }

    @PostMapping("/transfer")
    public Result transfer(@RequestBody @Valid TransferDTO transferDTO) {
        Long uid = requestInitService.uid();
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(transferDTO.getAmount());
        Currency currency = currencyService.get(uid, transferDTO.getFrom());
        if(currency.getRemain().compareTo(amount) < 0) ErrorCodeEnum.CREDIT_LACK.throwException();
        financialService.transfer(uid, transferDTO.getFrom(), transferDTO.getTo(), amount);
        return Result.instance();
    }

    @PostMapping("/transfer/token")
    public Result transferToken(@RequestBody @Valid TransferDTO transferDTO) {
        Long uid = requestInitService.uid();
        if(CurrencyTypeEnum.actual.equals(transferDTO.getFrom()) || CurrencyTypeEnum.actual.equals(transferDTO.getTo())){
            CurrencyToken currencyToken = currencyTokenService.get(uid, CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt);
            currencyTokenService.transfer(uid, transferDTO.getFrom(), transferDTO.getTo(), BigDecimal.valueOf(transferDTO.getAmount()), requestInitService.now().format(Constants.standardDateTimeFormatter));
        } else {
            Currency currency = currencyService.get(uid, transferDTO.getFrom());
            financialService.transfer(uid, transferDTO.getFrom(), transferDTO.getTo(), TokenCurrencyType.usdt_omni.amount(transferDTO.getAmount()));
        }
        return Result.instance();
    }

    @GetMapping("/my/financial")
    public Result my( @RequestParam(value = "page", defaultValue = "1") Integer page,
                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long uid = requestInitService.uid();
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.financial);
        LocalDate now = requestInitService.now().toLocalDate();
        List<UserFinancialPage> logList = userFinancialLogService.getUserFinancialPage(uid);
        List<UserFinancialPage> createdLogList = logList.stream().filter(o -> UserFinancialLogStatus.created.name().equals(o.getStatus())).collect(Collectors.toList());
        for(UserFinancialPage userFinancialPage: createdLogList) {
            double profit = 0.0;
            if(FinancialProductType.current.name().equals(userFinancialPage.getType())){
                long period = Math.max(0,userFinancialPage.getStart_date().until(now, DAYS));
                profit = TokenCurrencyType.usdt_omni.money(new BigDecimal(userFinancialPage.getAmount()).multiply(BigDecimal.valueOf(userFinancialPage.getRate() * period)).toBigInteger());
            }
            if(FinancialProductType.fixed.name().equals(userFinancialPage.getType())){
                long period = userFinancialPage.getStart_date().until(userFinancialPage.getEnd_date(), DAYS);
                long now_period = Math.max(userFinancialPage.getStart_date().until(requestInitService.now().toLocalDate(), DAYS),0);
                profit = TokenCurrencyType.usdt_omni.money(new BigDecimal(userFinancialPage.getAmount()).multiply(BigDecimal.valueOf(userFinancialPage.getRate() * Math.min(period, now_period))).toBigInteger());
            }
            userFinancialPage.setProfit(profit);
            userFinancialPage.setMoney(TokenCurrencyType.usdt_omni.money(userFinancialPage.getAmount()));
        }

        double got_profit1 = currencyLogService.list(new LambdaQueryWrapper<CurrencyLog>()
                .eq(CurrencyLog::getType,CurrencyTypeEnum.financial.name())
                .eq(CurrencyLog::getDes, CurrencyLogDes.赎回前扣除.name())
                .eq(CurrencyLog::getUid, uid)
        ).stream().mapToDouble(o->TokenCurrencyType.usdt_omni.money(o.getAmount())).sum();
        double got_profit2 = currencyLogService.list(new LambdaQueryWrapper<CurrencyLog>()
                .eq(CurrencyLog::getType,CurrencyTypeEnum.financial.name())
                .eq(CurrencyLog::getDes, CurrencyLogDes.赎回.name())
                .eq(CurrencyLog::getUid, uid)
        ).stream().mapToDouble(o->TokenCurrencyType.usdt_omni.money(o.getAmount())).sum();
        double got_profit = Math.max(got_profit2 - got_profit1, 0.0);
        double to_get_profit = createdLogList.stream().mapToDouble(UserFinancialPage::getProfit).sum();

        return Result.instance().setData(
                MapTool.Map().put("balance", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("remain", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("freeze", TokenCurrencyType.usdt_omni.money(currency.getFreeze()))
                .put("list", createdLogList.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList()))
                .put("all_profit", got_profit + to_get_profit)
                .put("count",createdLogList.size())
        );
    }

    @GetMapping("/my/bill")
    public Result myBill(LogPageDTO dto) {
        Long uid = requestInitService.uid();
        Wrapper<CurrencyLog> queryWrapper = new LambdaQueryWrapper<CurrencyLog>()
                .eq(CurrencyLog::getUid, uid)
                .in(CurrencyLog::getLog_type, Lists.newArrayList(CurrencyLogType.reduce, CurrencyLogType.increase, CurrencyLogType.freeze, CurrencyLogType.unfreeze))
                .eq(CurrencyLog::getType, CurrencyTypeEnum.financial)
                .ne(CurrencyLog::getDes, CurrencyLogDes.赎回前扣除.name())
                .eq(Objects.nonNull(dto.getDes()), CurrencyLog::getDes, dto.getDes())
                .orderByDesc(CurrencyLog::getId);
        Page<CurrencyLog> page = currencyLogService.page(new Page<>(dto.getPage(), dto.getSize()), queryWrapper);
        long count = currencyLogService.count(queryWrapper);
        List<LogPageVO> voList = page.getRecords().stream().map(LogPageVO::trans).collect(Collectors.toList());
        return Result.instance().setList(voList, count);
    }

    @PostMapping("/purchase")
    public Result purchase(@RequestBody @Valid PurchaseDTO purchaseDTO){
        Long uid = requestInitService.uid();
        FinancialProduct financialProduct = financialProductService.getById(purchaseDTO.getProduct_id());
        if(financialProduct == null || !FinancialProductStatus.enable.name().equals(financialProduct.getStatus())) ErrorCodeEnum.NOT_OPEN.throwException();

        BigInteger amount = TokenCurrencyType.usdt_omni.amount(purchaseDTO.getAmount());
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.financial);
        if(currency.getRemain().compareTo(amount) < 0) ErrorCodeEnum.CREDIT_LACK.throwException();

        financialService.purchase(financialProduct, amount, uid);
        return Result.instance();
    }


    @PostMapping("/withdraw/{user_financial_id}")
    public Result withdraw(@PathVariable("user_financial_id") Long user_financial_id){
        redisLock.lock("UserFinancialLogWithDraw." + user_financial_id,10L, TimeUnit.SECONDS);
        Long uid = requestInitService.uid();
        UserFinancialLog userFinancialLog = userFinancialLogService.getById(user_financial_id);
        if(UserFinancialLogStatus.success.name().equals(userFinancialLog.getStatus())) return Result.instance();
        if(!uid.equals(userFinancialLog.getUser_id())) ErrorCodeEnum.ACCESS_DENY.throwException();
        if(FinancialProductType.fixed.name().equals(userFinancialLog.getFinancial_product_type())
                && userFinancialLog.getEnd_date().isAfter(requestInitService.now().toLocalDate())
        ) ErrorCodeEnum.ACCESS_DENY.throwException();
        financialService.withdraw(userFinancialLog);
        return Result.instance();
    }


//    @GetMapping("/day/past/{days}")
//    public Result dayPast(@PathVariable("days") Long day){
//        List<UserFinancialLog> list = userFinancialLogService.list(new LambdaQueryWrapper<UserFinancialLog>()
//                .eq(UserFinancialLog::getStatus, UserFinancialLogStatus.created));
//        for(UserFinancialLog u: list){
//            u.setStart_date(u.getStart_date().minusDays(day));
//            u.setEnd_date(u.getEnd_date().minusDays(day));
//        }
//        userFinancialLogService.updateBatchById(list);
//        return Result.instance();
//    }



    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private FinancialService financialService;
    @Resource
    private UserFinancialLogService userFinancialLogService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private CurrencyLogService currencyLogService;
    @Resource
    private CurrencyTokenService currencyTokenService;
}
