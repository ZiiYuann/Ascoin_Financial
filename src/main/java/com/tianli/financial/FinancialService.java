package com.tianli.financial;

import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.sso.init.RequestInitService;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.financial.mapper.FinancialProduct;
import com.tianli.financial.mapper.FinancialProductType;
import com.tianli.financial.mapper.UserFinancialLog;
import com.tianli.financial.mapper.UserFinancialLogStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class FinancialService {

    @Transactional
    public void transfer(Long uid, CurrencyTypeEnum from, CurrencyTypeEnum to, BigInteger amount) {
        currencyService.transfer(uid, from, to, amount, requestInitService.now().format(Constants.standardDateTimeFormatter));
    }

    @Transactional
    public void purchase(FinancialProduct financialProduct, BigInteger amount, Long uid) {
        Long id = CommonFunction.generalId();
        currencyService.freeze(uid, CurrencyTypeEnum.financial, amount, id.toString(), CurrencyLogDes.买入.name());
        LocalDate start_date = requestInitService.now().toLocalDate().plusDays(1L);
        UserFinancialLog userFinancialLog = UserFinancialLog.builder()
                .financial_product_id(financialProduct.getId())
                .user_id(uid).financial_product_type(financialProduct.getType())
                .amount(amount).create_time(requestInitService.now()).start_date(start_date)
                .end_date(start_date.plusDays(financialProduct.getPeriod()))
                .finish_amount(BigInteger.ZERO)
                .id(id).rate(financialProduct.getRate()).status(UserFinancialLogStatus.created.name())
                .build();
        userFinancialLogService.save(userFinancialLog);
    }

    @Transactional
    public void withdraw(UserFinancialLog userFinancialLog) {
        BigInteger amount = userFinancialLog.getAmount();
        LocalDate now = requestInitService.now().toLocalDate();
        long period = 0L;
        BigInteger profit = BigInteger.ZERO;
        if(FinancialProductType.current.name().equals(userFinancialLog.getFinancial_product_type())){
            period = Math.max(userFinancialLog.getStart_date().until(now, DAYS), 0);
            profit = new BigDecimal(amount).multiply(BigDecimal.valueOf(userFinancialLog.getRate() * period)).toBigInteger();
        }
        if(FinancialProductType.fixed.name().equals(userFinancialLog.getFinancial_product_type())) {
            period = userFinancialLog.getStart_date().until(userFinancialLog.getEnd_date(), DAYS);
            profit = new BigDecimal(amount).multiply(BigDecimal.valueOf(userFinancialLog.getRate() * period)).toBigInteger();
        }
        userFinancialLog.setStatus(UserFinancialLogStatus.success.name());
        userFinancialLog.setFinish_time(requestInitService.now());
        userFinancialLog.setFinish_amount(amount.add(profit));
        userFinancialLogService.updateById(userFinancialLog);
        currencyService.reduce(userFinancialLog.getUser_id(), CurrencyTypeEnum.financial, amount, userFinancialLog.getId().toString(), CurrencyLogDes.赎回前扣除.name());
        currencyService.increase(userFinancialLog.getUser_id(), CurrencyTypeEnum.financial, amount.add(profit), userFinancialLog.getId().toString(), CurrencyLogDes.赎回.name());
    }


    @Resource
    private CurrencyService currencyService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private UserFinancialLogService userFinancialLogService;
}
