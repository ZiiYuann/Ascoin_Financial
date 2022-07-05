package com.tianli.management.ruleconfig;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tianli.bet.BFRewardConfig;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.kline.mapper.FollowCurrencyMapper;
import com.tianli.mconfig.mapper.ConfigMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecialParamConfigVO {

    /**
     * 门槛设置
     */
    private String bet_warning_amount;

    /**
     * 每日奖励 U 数额
     */
    private String daily_gift_amount;

    /**
     * 新人奖励 U 数额
     */
    private String new_gift_amount;

    private String kyc_award;

    /**
     * 币种设置
     */
    private List<CurrencyConfig> currency;

    /**
     * 奖励配置
     */
    private List<BFRewardConfig> rewardConfigList;

    public static SpecialParamConfigVO build(ConfigMapper configMapper, FollowCurrencyMapper followCurrencyMapper) {
        String bf_constant_reward_json = configMapper.getParam(ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_KEY);
        if(StringUtils.isBlank(bf_constant_reward_json)){
            bf_constant_reward_json = ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_JSON;
        }

        List<BFRewardConfig> list = new Gson().fromJson(bf_constant_reward_json, new TypeToken<List<BFRewardConfig>>() {
        }.getType());
        String dailyGiftAmount = configMapper.getParam(ConfigConstants.DAILY_GIFT_AMOUNT);
        dailyGiftAmount = StringUtils.isBlank(dailyGiftAmount) ? "10" : dailyGiftAmount;
        String newGiftAmount = configMapper.getParam(ConfigConstants.NEW_GIFT_AMOUNT);
        newGiftAmount = StringUtils.isBlank(newGiftAmount) ? "50" : newGiftAmount;
        String betWarningAmount = configMapper.getParam(ConfigConstants.BET_WARNING_AMOUNT);
        betWarningAmount = StringUtils.isBlank(betWarningAmount) ? "1000" : betWarningAmount;
        String kyc_award = configMapper.getParam(ConfigConstants.KYC_AWARD);
        kyc_award = StrUtil.isBlank(kyc_award) ? "0" :kyc_award;
        return SpecialParamConfigVO.builder()
                .bet_warning_amount(betWarningAmount)
                .daily_gift_amount(dailyGiftAmount)
                .new_gift_amount(newGiftAmount)
                .kyc_award(kyc_award)
                .currency(followCurrencyMapper.selectList(new LambdaQueryWrapper<FollowCurrency>()
                        .orderByAsc(FollowCurrency::getSort))
                        .stream()
                        .map(CurrencyConfig::trans)
                        .collect(Collectors.toList()))
                .rewardConfigList(list.stream().sorted((o1, o2) -> (int) (o1.getMin() - o2.getMin()) * 100).collect(Collectors.toList()))
                .build();
    }
}
