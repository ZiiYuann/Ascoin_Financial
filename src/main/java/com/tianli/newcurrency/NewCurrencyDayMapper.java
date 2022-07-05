package com.tianli.newcurrency;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 每天0点用户币余额 Mapper 接口
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Mapper
public interface NewCurrencyDayMapper extends BaseMapper<NewCurrencyDay> {

    @Select("select id,uid,type,token,balance,freeze,remain from currency_token where token ='usdt'")
    List<NewCurrencyDay> getCurrencyByDay();

}
