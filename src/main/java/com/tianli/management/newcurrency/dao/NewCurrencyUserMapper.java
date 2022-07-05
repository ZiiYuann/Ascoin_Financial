package com.tianli.management.newcurrency.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.newcurrency.entity.NewCurrencySumDTO;
import com.tianli.management.newcurrency.entity.NewCurrencyUser;
import com.tianli.newcurrency.NewCurrencyDayDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 新币用户表 Mapper 接口
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Mapper
public interface NewCurrencyUserMapper extends BaseMapper<NewCurrencyUser> {

    NewCurrencySumDTO sumNewCurrency();

    NewCurrencySumDTO sumNewCurrencyByCurrencyName(Long currencyId);

    NewCurrencyDayDTO laungchpad();

    List<NewCurrencySumDTO> selectNewCurrency();
}
