package com.tianli.newcurrency;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.entity.NewCurrencyUser;

/**
 * <p>
 * 每天0点用户币余额 服务类
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
public interface INewCurrencyDayService extends IService<NewCurrencyDay> {

    void syncSaveCurrency();

    Result selectNewCurrecy(Long currencyId,Long uid);

    Result getNewCurrency();

    Result tradingRecord(Long uid,Long page, Long size);

    Result laungchpad(Long page, Long size);

    Result inputConfirm(NewCurrencyUser newCurrencyUser);

    void syncComputedCurrency();
}
