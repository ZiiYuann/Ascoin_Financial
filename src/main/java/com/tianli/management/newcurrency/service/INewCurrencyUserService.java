package com.tianli.management.newcurrency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.entity.NewCurrencySumDTO;
import com.tianli.management.newcurrency.entity.NewCurrencyUser;
import com.tianli.management.newcurrency.entity.NewCurrencyUserDTO;
import com.tianli.newcurrency.NewCurrencyDayDTO;

import java.util.List;

/**
 * <p>
 * 新币用户表 服务类
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
public interface INewCurrencyUserService extends IService<NewCurrencyUser> {

    Result page(NewCurrencyUserDTO new_currency_user, Long page, Long size);

    Result sumNewCurrency();

    NewCurrencySumDTO sumNewCurrencyByCurrencyName(Long currencyId);

    NewCurrencyDayDTO laungchpad();

    List<NewCurrencySumDTO> selectNewCurrency();
}
