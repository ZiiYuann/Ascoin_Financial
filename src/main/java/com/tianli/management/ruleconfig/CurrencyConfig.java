package com.tianli.management.ruleconfig;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.common.DoubleDecimalTrans;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.kline.mapper.FollowCurrencyMapper;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @author chensong
 * @date 2021-03-01 14:23
 * @since 1.0.0
 */
@Data
@Builder
public class CurrencyConfig {
    private String name;
    private Boolean selected;
    private Double win_rate;

    public static CurrencyConfig trans(FollowCurrency followCurrency){
        return CurrencyConfig.builder()
                .name(followCurrency.getName())
                .selected(followCurrency.getSelected())
                .win_rate(DoubleDecimalTrans.double_multiply_hundred(followCurrency.getWin_rate())).build();
    }

    public void updateByName(FollowCurrencyMapper followCurrencyMapper) {
        followCurrencyMapper.update(null, new LambdaUpdateWrapper<FollowCurrency>()
                .eq(FollowCurrency::getName, name)
                .set(FollowCurrency::getSelected, selected)
                .set(FollowCurrency::getWin_rate, Objects.isNull(win_rate) ? -0.01 : DoubleDecimalTrans.double_divide_hundred(win_rate)));
    }
}
