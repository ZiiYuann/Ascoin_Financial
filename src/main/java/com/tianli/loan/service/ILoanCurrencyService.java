package com.tianli.loan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.entity.LoanCurrency;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 用户贷款余额表 服务类
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
public interface ILoanCurrencyService extends IService<LoanCurrency> {

    /**
     * 增加金额
     *
     * @param loan
     */
    void increase(Loan loan);


    LoanCurrency findByUid(Long uid, CurrencyCoinEnum currencyCoinEnum);

    /**
     * 扣减余额
     *
     * @param uid
     * @param amount
     * @param usdt
     * @param generalId
     * @param dsc
     */
    void reduce(Long uid, BigInteger amount, CurrencyCoinEnum usdt, long generalId, String dsc);

    /**
     * 押注扣款
     * @param uid
     * @param amount
     * @param token
     * @param generalId
     * @param des
     * @return
     */
    BigInteger betAmountReduce(Long uid, BigInteger amount, CurrencyCoinEnum token, long generalId, String des);

    void increase(Long uid, BigInteger loan_amount, CurrencyCoinEnum token, Long id, String des);

    /**
     * 押注结算
     * @param uid
     * @param id
     * @param usdt
     * @param returnAmount
     * @param subtract
     * @param name
     * @return
     */
    BigInteger betIncrease(Long uid, Long id, CurrencyCoinEnum usdt, BigInteger returnAmount, BigInteger subtract, String name);

    /**
     * 批量查询用户贷款余额
     * @param userIds
     * @param currencyCoinEnum
     * @return
     */
    List<LoanCurrency> findByUids(List<Long> userIds, CurrencyCoinEnum currencyCoinEnum);
}
