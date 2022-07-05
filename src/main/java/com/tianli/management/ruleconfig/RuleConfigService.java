package com.tianli.management.ruleconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.common.DoubleDecimalTrans;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.kline.mapper.FollowCurrencyMapper;
import com.tianli.management.ruleconfig.mapper.BetDuration;
import com.tianli.management.ruleconfig.mapper.BetDurationMapper;
import com.tianli.mconfig.mapper.Config;
import com.tianli.mconfig.mapper.ConfigMapper;
import com.tianli.tool.RateTransTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RuleConfigService {
    public List<BetDuration> selectAll() {
        return betDurationMapper.selectAll();
    }

    public int updateDuration(BetDurationDTO betDuration) {
        /*if(betDuration.getMax_bet_amount() < betDuration.getMin_bet_amount()){
            ErrorCodeEnum.throwException("最大押注金额要大于最小押注金额");
        }*/
        return betDurationMapper.updateDuration(betDuration.getId(), betDuration.getDuration(), TokenCurrencyType.usdt_omni.amount(betDuration.getMin_bet_amount()), TokenCurrencyType.usdt_omni.amount(betDuration.getMax_bet_amount()), betDuration.getExtra_percentage());
    }

    @Transactional
    public boolean updateParam(ParamConfigDTO paramConfig) {
        configMapper.replaceParam(ConfigConstants.BET_RAKE_RATE_NORMAL, RateTransTool.div(paramConfig.getBet_rake_rate_normal()));
        configMapper.replaceParam(ConfigConstants.BET_RAKE_RATE_STEADY,RateTransTool.div(paramConfig.getBet_rake_rate_steady()));
        configMapper.replaceParam(ConfigConstants.PROPORTION_OF_FIRST_REBATE,RateTransTool.div(paramConfig.getProportion_of_first_rebate()));
        configMapper.replaceParam(ConfigConstants.PROPORTION_OF_SECOND_REBATE,RateTransTool.div(paramConfig.getProportion_of_second_rebate()));
        configMapper.replaceParam(ConfigConstants.PLATFORM_REBATE_RATE_NORMAL,RateTransTool.div(paramConfig.getPlatform_rebate_rate_normal()));
        configMapper.replaceParam(ConfigConstants.PLATFORM_REBATE_RATE_STEADY,RateTransTool.div(paramConfig.getPlatform_rebate_rate_steady()));
//        configMapper.replaceParam(ConfigConstants.USER_BALANCE_DAILY_RATE,RateTransTool.div(paramConfig.getUser_balance_daily_rate()));
        configMapper.replaceParam(ConfigConstants.BF_USDT_RATE,paramConfig.getBF_usdt_rate());
        configMapper.replaceParam(ConfigConstants.USDT_BF_DISCOUNT_RATE,RateTransTool.div(paramConfig.getUsdt_BF_discount_rate()));
        configMapper.replaceParam(ConfigConstants.BF_SWITCH_MIN_AMOUNT,paramConfig.getBF_switch_min_amount());
        configMapper.replaceParam(ConfigConstants.USDT_ERC20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdt_erc20_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_ERC20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdt_erc20.moneyStr2AmountStr(paramConfig.getUsdt_erc20_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_ERC20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdt_erc20.moneyStr2AmountStr(paramConfig.getUsdt_erc20_withdraw_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_BEP20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdt_bep20_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_BEP20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdt_bep20.moneyStr2AmountStr(paramConfig.getUsdt_bep20_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_BEP20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdt_bep20.moneyStr2AmountStr(paramConfig.getUsdt_bep20_withdraw_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_OMNI_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdt_omni_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_OMNI_WITHDRAW_FIXED_AMOUNT,TokenCurrencyType.usdt_omni.moneyStr2AmountStr(paramConfig.getUsdt_omni_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_OMNI_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdt_omni.moneyStr2AmountStr(paramConfig.getUsdt_omni_withdraw_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_TRC20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdt_trc20_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_TRC20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdt_trc20.moneyStr2AmountStr(paramConfig.getUsdt_trc20_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_TRC20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdt_trc20.moneyStr2AmountStr(paramConfig.getUsdt_trc20_withdraw_min_amount()));

        configMapper.replaceParam(ConfigConstants.USDC_ERC20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdc_erc20_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDC_ERC20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdc_erc20.moneyStr2AmountStr(paramConfig.getUsdc_erc20_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDC_ERC20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdc_erc20.moneyStr2AmountStr(paramConfig.getUsdc_erc20_withdraw_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDC_BEP20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdc_bep20_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDC_BEP20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdc_bep20.moneyStr2AmountStr(paramConfig.getUsdc_bep20_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDC_BEP20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdc_bep20.moneyStr2AmountStr(paramConfig.getUsdc_bep20_withdraw_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDC_TRC20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdc_trc20_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.USDC_TRC20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdc_trc20.moneyStr2AmountStr(paramConfig.getUsdc_trc20_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDC_TRC20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdc_trc20.moneyStr2AmountStr(paramConfig.getUsdc_trc20_withdraw_min_amount()));

        configMapper.replaceParam(ConfigConstants.BF_BEP20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getBF_withdraw_rate()));
        configMapper.replaceParam(ConfigConstants.BF_BEP20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.BF_bep20.moneyStr2AmountStr(paramConfig.getBF_withdraw_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.BF_BEP20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.BF_bep20.moneyStr2AmountStr(paramConfig.getBF_withdraw_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_ERC20_AGENT_SETTLE_RATE,RateTransTool.div(paramConfig.getUsdt_erc20_agent_settle_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_ERC20_AGENT_SETTLE_FIXED_AMOUNT,TokenCurrencyType.usdt_erc20.moneyStr2AmountStr(paramConfig.getUsdt_erc20_agent_settle_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_ERC20_AGENT_SETTLE_MIN_AMOUNT,TokenCurrencyType.usdt_erc20.moneyStr2AmountStr(paramConfig.getUsdt_erc20_agent_settle_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_BEP20_AGENT_SETTLE_RATE,RateTransTool.div(paramConfig.getUsdt_bep20_agent_settle_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_BEP20_AGENT_SETTLE_FIXED_AMOUNT,TokenCurrencyType.usdt_bep20.moneyStr2AmountStr(paramConfig.getUsdt_bep20_agent_settle_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_BEP20_AGENT_SETTLE_MIN_AMOUNT,TokenCurrencyType.usdt_bep20.moneyStr2AmountStr(paramConfig.getUsdt_bep20_agent_settle_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_OMNI_AGENT_SETTLE_RATE,RateTransTool.div(paramConfig.getUsdt_omni_agent_settle_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_OMNI_AGENT_SETTLE_FIXED_AMOUNT,TokenCurrencyType.usdt_omni.moneyStr2AmountStr(paramConfig.getUsdt_omni_agent_settle_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_OMNI_AGENT_SETTLE_MIN_AMOUNT,TokenCurrencyType.usdt_omni.moneyStr2AmountStr(paramConfig.getUsdt_omni_agent_settle_min_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_TRC20_AGENT_SETTLE_RATE,RateTransTool.div(paramConfig.getUsdt_trc20_agent_settle_rate()));
        configMapper.replaceParam(ConfigConstants.USDT_TRC20_AGENT_SETTLE_FIXED_AMOUNT,TokenCurrencyType.usdt_trc20.moneyStr2AmountStr(paramConfig.getUsdt_trc20_agent_settle_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.USDT_TRC20_AGENT_SETTLE_MIN_AMOUNT,TokenCurrencyType.usdt_trc20.moneyStr2AmountStr(paramConfig.getUsdt_trc20_agent_settle_min_amount()));
        configMapper.replaceParam(ConfigConstants.BF_AGENT_SETTLE_RATE,RateTransTool.div(paramConfig.getBF_agent_settle_rate()));
        configMapper.replaceParam(ConfigConstants.BF_AGENT_SETTLE_FIXED_AMOUNT,TokenCurrencyType.BF_bep20.moneyStr2AmountStr(paramConfig.getBF_agent_settle_fixed_amount()));
        configMapper.replaceParam(ConfigConstants.BF_AGENT_SETTLE_MIN_AMOUNT,TokenCurrencyType.BF_bep20.moneyStr2AmountStr(paramConfig.getBF_agent_settle_min_amount()));
        configMapper.replaceParam(ConfigConstants.RECORD_TO_BE_COLLECTED_RATE,RateTransTool.div(paramConfig.getRecord_to_be_collected_rate()));
        configMapper.replaceParam(ConfigConstants.KYC_TRIGGER_AMOUNT, paramConfig.getKyc_trigger_amount());
        configMapper.replaceParam(ConfigConstants.ACTUAL_BUY_RATE,paramConfig.getActual_buy_rate());
        configMapper.replaceParam(ConfigConstants.ACTUAL_SELL_RATE,paramConfig.getActual_sell_rate());
        configMapper.replaceParam(ConfigConstants.ACTUAL_WITHDRAW_RATE,paramConfig.getActual_withdraw_rate());
        if(Objects.isNull(paramConfig.getCurrency())){
            return true;
        }
        for (CurrencyConfig currencyConfig : paramConfig.getCurrency()) {
            followCurrencyMapper.update(null, new LambdaUpdateWrapper<FollowCurrency>()
                    .eq(FollowCurrency::getName, currencyConfig.getName())
                    .set(FollowCurrency::getSelected, currencyConfig.getSelected())
                    .set(FollowCurrency::getWin_rate, Objects.isNull(currencyConfig.getWin_rate()) ? -0.01 : DoubleDecimalTrans.double_divide_hundred(currencyConfig.getWin_rate())));
        }
        return true;
    }

    public ParamConfigVO paramList() {
        List<FollowCurrency> list = followCurrencyMapper.selectList(new LambdaQueryWrapper<FollowCurrency>().orderByAsc(FollowCurrency::getSort));
        List<CurrencyConfig> vo = list.stream().map(CurrencyConfig::trans).collect(Collectors.toList());
        Map<String, String> configMap = configMapper.getAllParams().stream().collect(Collectors.toMap(Config::getName, Config::getValue));
        ParamConfigVO paramConfig = ParamConfigVO.builder()
                // 押注赢钱抽佣比例（普通场）
                .bet_rake_rate_normal(RateTransTool.multi(configMap.get(ConfigConstants.BET_RAKE_RATE_NORMAL)))
                // 押注赢钱抽佣比例（稳赚场）
                .bet_rake_rate_steady(RateTransTool.multi(configMap.get(ConfigConstants.BET_RAKE_RATE_STEADY)))
                // 初级返佣比例
                .proportion_of_first_rebate(RateTransTool.multi(configMap.get(ConfigConstants.PROPORTION_OF_FIRST_REBATE)))
                // 次级返佣比例
                .proportion_of_second_rebate(RateTransTool.multi(configMap.get(ConfigConstants.PROPORTION_OF_SECOND_REBATE)))
                // 平台结算比例（普通场）
                .platform_rebate_rate_normal(RateTransTool.multi(configMap.get(ConfigConstants.PLATFORM_REBATE_RATE_NORMAL)))
                // 平台结算比例（稳赚场）
                .platform_rebate_rate_steady(RateTransTool.multi(configMap.get(ConfigConstants.PLATFORM_REBATE_RATE_STEADY)))
                // 用户余额日利率
                .user_balance_daily_rate(RateTransTool.multi(configMap.get(ConfigConstants.USER_BALANCE_DAILY_RATE)))
                // BF/USDT转化比例
                .BF_usdt_rate(configMap.get(ConfigConstants.BF_USDT_RATE))
                // BF折扣门槛额
                .BF_switch_min_amount(configMap.get(ConfigConstants.BF_SWITCH_MIN_AMOUNT))
                .usdt_BF_discount_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_BF_DISCOUNT_RATE)))
                // 用户提现手续费（TRC20）
                .usdt_trc20_withdraw_min_amount(TokenCurrencyType.usdt_trc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_TRC20_WITHDRAW_MIN_AMOUNT)))
                .usdt_trc20_withdraw_fixed_amount(TokenCurrencyType.usdt_trc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_TRC20_WITHDRAW_FIXED_AMOUNT)))
                .usdt_trc20_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_TRC20_WITHDRAW_RATE)))
                // 用户提现手续费（ERC20）
                .usdt_erc20_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_ERC20_WITHDRAW_RATE)))
                .usdt_erc20_withdraw_fixed_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_ERC20_WITHDRAW_FIXED_AMOUNT)))
                .usdt_erc20_withdraw_min_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_ERC20_WITHDRAW_MIN_AMOUNT)))
                // 用户提现手续费（OMNI）
                .usdt_omni_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_OMNI_WITHDRAW_RATE)))
                .usdt_omni_withdraw_fixed_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_OMNI_WITHDRAW_FIXED_AMOUNT)))
                .usdt_omni_withdraw_min_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_OMNI_WITHDRAW_MIN_AMOUNT)))
                // 用户提现手续费（BSC）
                .usdt_bep20_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_BEP20_WITHDRAW_RATE)))
                .usdt_bep20_withdraw_fixed_amount(TokenCurrencyType.usdt_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_BEP20_WITHDRAW_FIXED_AMOUNT)))
                .usdt_bep20_withdraw_min_amount(TokenCurrencyType.usdt_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_BEP20_WITHDRAW_MIN_AMOUNT)))
                // 用户提现手续费（usdc_TRC20）
                .usdc_trc20_withdraw_min_amount(TokenCurrencyType.usdc_trc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDC_TRC20_WITHDRAW_MIN_AMOUNT)))
                .usdc_trc20_withdraw_fixed_amount(TokenCurrencyType.usdc_trc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDC_TRC20_WITHDRAW_FIXED_AMOUNT)))
                .usdc_trc20_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDC_TRC20_WITHDRAW_RATE)))
                // 用户提现手续费（usdc_ERC20）
                .usdc_erc20_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDC_ERC20_WITHDRAW_RATE)))
                .usdc_erc20_withdraw_fixed_amount(TokenCurrencyType.usdc_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDC_ERC20_WITHDRAW_FIXED_AMOUNT)))
                .usdc_erc20_withdraw_min_amount(TokenCurrencyType.usdc_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDC_ERC20_WITHDRAW_MIN_AMOUNT)))
                // 用户提现手续费（usdc_BSC）
                .usdc_bep20_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDC_BEP20_WITHDRAW_RATE)))
                .usdc_bep20_withdraw_fixed_amount(TokenCurrencyType.usdc_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDC_BEP20_WITHDRAW_FIXED_AMOUNT)))
                .usdc_bep20_withdraw_min_amount(TokenCurrencyType.usdc_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDC_BEP20_WITHDRAW_MIN_AMOUNT)))
                // 用户提现手续费（BF）
                .BF_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.BF_BEP20_WITHDRAW_RATE)))
                .BF_withdraw_fixed_amount(TokenCurrencyType.BF_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.BF_BEP20_WITHDRAW_FIXED_AMOUNT)))
                .BF_withdraw_min_amount(TokenCurrencyType.BF_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.BF_BEP20_WITHDRAW_MIN_AMOUNT)))
//                .user_rebate_rate_normal(RateTransTool.multi(configMap.get(ConfigConstants.USER_REBATE_RATE_NORMAL)))
//                .user_rebate_rate_steady(RateTransTool.multi(configMap.get(ConfigConstants.USER_REBATE_RATE_STEADY)))
                // 代理商结算手续费（ERC20）
                .usdt_erc20_agent_settle_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_ERC20_AGENT_SETTLE_RATE)))
                .usdt_erc20_agent_settle_fixed_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_ERC20_AGENT_SETTLE_FIXED_AMOUNT)))
                .usdt_erc20_agent_settle_min_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_ERC20_AGENT_SETTLE_MIN_AMOUNT)))
                // 代理商结算手续费（OMNI）
                .usdt_omni_agent_settle_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_OMNI_AGENT_SETTLE_RATE)))
                .usdt_omni_agent_settle_fixed_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_OMNI_AGENT_SETTLE_FIXED_AMOUNT)))
                .usdt_omni_agent_settle_min_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_OMNI_AGENT_SETTLE_MIN_AMOUNT)))
                // 代理商结算手续费（TRC20）
                .usdt_trc20_agent_settle_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_RATE)))
                .usdt_trc20_agent_settle_fixed_amount(TokenCurrencyType.usdt_trc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_FIXED_AMOUNT)))
                .usdt_trc20_agent_settle_min_amount(TokenCurrencyType.usdt_trc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_TRC20_AGENT_SETTLE_MIN_AMOUNT)))
                // 代理商结算手续费（BEP20）
                .usdt_bep20_agent_settle_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_RATE)))
                .usdt_bep20_agent_settle_fixed_amount(TokenCurrencyType.usdt_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_FIXED_AMOUNT)))
                .usdt_bep20_agent_settle_min_amount(TokenCurrencyType.usdt_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_BEP20_AGENT_SETTLE_MIN_AMOUNT)))
                // 代理商结算手续费（BF）
                .BF_agent_settle_rate(RateTransTool.multi(configMap.get(ConfigConstants.BF_AGENT_SETTLE_RATE)))
                .BF_agent_settle_fixed_amount(TokenCurrencyType.BF_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.BF_AGENT_SETTLE_FIXED_AMOUNT)))
                .BF_agent_settle_min_amount(TokenCurrencyType.BF_bep20.amountStr2MoneyStr(configMap.get(ConfigConstants.BF_AGENT_SETTLE_MIN_AMOUNT)))
                .kyc_trigger_amount(configMap.getOrDefault(ConfigConstants.KYC_TRIGGER_AMOUNT, "10000"))
                .actual_buy_rate(configMap.get(ConfigConstants.ACTUAL_BUY_RATE))
                .actual_sell_rate(configMap.get(ConfigConstants.ACTUAL_SELL_RATE))
                .actual_withdraw_rate(configMap.get(ConfigConstants.ACTUAL_WITHDRAW_RATE))
//                 撤销保证金
//                .usdt_erc20_agent_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_ERC20_AGENT_WITHDRAW_RATE)))
//                .usdt_erc20_agent_withdraw_fixed_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_ERC20_AGENT_WITHDRAW_FIXED_AMOUNT)))
//                .usdt_erc20_agent_withdraw_min_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_ERC20_AGENT_WITHDRAW_MIN_AMOUNT)))
//                .usdt_omni_agent_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_OMNI_AGENT_WITHDRAW_RATE)))
//                .usdt_omni_agent_withdraw_fixed_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_OMNI_AGENT_WITHDRAW_FIXED_AMOUNT)))
//                .usdt_omni_agent_withdraw_min_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_OMNI_AGENT_WITHDRAW_MIN_AMOUNT)))
//                .usdt_trc20_agent_withdraw_rate(RateTransTool.multi(configMap.get(ConfigConstants.USDT_TRC20_AGENT_WITHDRAW_RATE)))
//                .usdt_trc20_agent_withdraw_fixed_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_TRC20_AGENT_WITHDRAW_FIXED_AMOUNT)))
//                .usdt_trc20_agent_withdraw_min_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configMap.get(ConfigConstants.USDT_TRC20_AGENT_WITHDRAW_MIN_AMOUNT)))
                .record_to_be_collected_rate(RateTransTool.multi(configMap.get(ConfigConstants.RECORD_TO_BE_COLLECTED_RATE)))
                .currency(vo).build();

        return paramConfig;
    }

    @Transactional
    public boolean updateParam2(ParamConfig paramConfig) {
        configMapper.updateParam(ConfigConstants.BET_RAKE_RATE_NORMAL, RateTransTool.div(paramConfig.getBet_rake_rate_normal()));
        configMapper.updateParam(ConfigConstants.BET_RAKE_RATE_STEADY,RateTransTool.div(paramConfig.getBet_rake_rate_steady()));
        configMapper.updateParam(ConfigConstants.USER_REBATE_RATE_NORMAL,RateTransTool.div(paramConfig.getUser_rebate_rate_normal()));
        configMapper.updateParam(ConfigConstants.USER_REBATE_RATE_STEADY,RateTransTool.div(paramConfig.getUser_rebate_rate_steady()));
        configMapper.updateParam(ConfigConstants.USER_BALANCE_DAILY_RATE,RateTransTool.div(paramConfig.getUser_balance_daily_rate()));
        configMapper.updateParam(ConfigConstants.USDT_ERC20_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdt_erc20_withdraw_rate()));
        configMapper.updateParam(ConfigConstants.USDT_ERC20_WITHDRAW_FIXED_AMOUNT, TokenCurrencyType.usdt_erc20.moneyStr2AmountStr(paramConfig.getUsdt_erc20_withdraw_fixed_amount()));
        configMapper.updateParam(ConfigConstants.USDT_ERC20_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdt_erc20.moneyStr2AmountStr(paramConfig.getUsdt_erc20_withdraw_min_amount()));
        configMapper.updateParam(ConfigConstants.USDT_OMNI_WITHDRAW_RATE,RateTransTool.div(paramConfig.getUsdt_omni_withdraw_rate()));
        configMapper.updateParam(ConfigConstants.USDT_OMNI_WITHDRAW_FIXED_AMOUNT,TokenCurrencyType.usdt_omni.moneyStr2AmountStr(paramConfig.getUsdt_omni_withdraw_fixed_amount()));
        configMapper.updateParam(ConfigConstants.USDT_OMNI_WITHDRAW_MIN_AMOUNT,TokenCurrencyType.usdt_omni.moneyStr2AmountStr(paramConfig.getUsdt_omni_withdraw_min_amount()));
        configMapper.updateParam(ConfigConstants.PLATFORM_REBATE_RATE_NORMAL,RateTransTool.div(paramConfig.getPlatform_rebate_rate_normal()));
        configMapper.updateParam(ConfigConstants.PLATFORM_REBATE_RATE_STEADY,RateTransTool.div(paramConfig.getPlatform_rebate_rate_steady()));
        configMapper.updateParam(ConfigConstants.SUPER_AGENT_RAKE_RATE,RateTransTool.div(paramConfig.getSuper_agent_rake_rate()));
        configMapper.updateParam(ConfigConstants.SUPER_AGENT_REFERRAL,paramConfig.getSuper_agent_referral());
        configMapper.updateParam(ConfigConstants.SUPER_AGENT_SUBORDINATE,paramConfig.getSuper_agent_subordinate());
        configMapper.updateParam(ConfigConstants.SUPER_AGENT_SUBORDINATE_REFERRAL,paramConfig.getSuper_agent_subordinate_referral());
        if(Objects.isNull(paramConfig.getCurrency())){
            return true;
        }
        for (CurrencyConfig currencyConfig : paramConfig.getCurrency()) {
            followCurrencyMapper.update(null, new LambdaUpdateWrapper<FollowCurrency>()
                    .eq(FollowCurrency::getName, currencyConfig.getName())
                    .set(FollowCurrency::getSelected, currencyConfig.getSelected()));
        }
        return true;
    }

    public ParamConfig paramList2() {
        List<FollowCurrency> list = followCurrencyMapper.selectList(new LambdaQueryWrapper<FollowCurrency>().orderByAsc(FollowCurrency::getSort));
        List<CurrencyConfig> vo = list.stream().map(CurrencyConfig::trans).collect(Collectors.toList());
        List<Config> allParam = configMapper.getAllParams();
        Map<String, String> configCacheMap = allParam.stream().collect(Collectors.toMap(Config::getName, Config::getValue));
        ParamConfig paramConfig = ParamConfig.builder()
                .bet_rake_rate_normal(RateTransTool.multi(configCacheMap.get(ConfigConstants.BET_RAKE_RATE_NORMAL)))
                .bet_rake_rate_steady(RateTransTool.multi(configCacheMap.get(ConfigConstants.BET_RAKE_RATE_STEADY)))
                .user_rebate_rate_normal(RateTransTool.multi(configCacheMap.get(ConfigConstants.USER_REBATE_RATE_NORMAL)))
                .user_rebate_rate_steady(RateTransTool.multi(configCacheMap.get(ConfigConstants.USER_REBATE_RATE_STEADY)))
                .user_balance_daily_rate(RateTransTool.multi(configCacheMap.get(ConfigConstants.USER_BALANCE_DAILY_RATE)))
                .usdt_erc20_withdraw_rate(RateTransTool.multi(configCacheMap.get(ConfigConstants.USDT_ERC20_WITHDRAW_RATE)))
                .usdt_erc20_withdraw_fixed_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configCacheMap.get(ConfigConstants.USDT_ERC20_WITHDRAW_FIXED_AMOUNT)))
                .usdt_erc20_withdraw_min_amount(TokenCurrencyType.usdt_erc20.amountStr2MoneyStr(configCacheMap.get(ConfigConstants.USDT_ERC20_WITHDRAW_MIN_AMOUNT)))
                .usdt_omni_withdraw_rate(RateTransTool.multi(configCacheMap.get(ConfigConstants.USDT_OMNI_WITHDRAW_RATE)))
                .usdt_omni_withdraw_fixed_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configCacheMap.get(ConfigConstants.USDT_OMNI_WITHDRAW_FIXED_AMOUNT)))
                .usdt_omni_withdraw_min_amount(TokenCurrencyType.usdt_omni.amountStr2MoneyStr(configCacheMap.get(ConfigConstants.USDT_OMNI_WITHDRAW_MIN_AMOUNT)))
                .platform_rebate_rate_normal(RateTransTool.multi(configCacheMap.get(ConfigConstants.PLATFORM_REBATE_RATE_NORMAL)))
                .platform_rebate_rate_steady(RateTransTool.multi(configCacheMap.get(ConfigConstants.PLATFORM_REBATE_RATE_STEADY)))
                .super_agent_rake_rate(RateTransTool.multi(configCacheMap.get(ConfigConstants.SUPER_AGENT_RAKE_RATE)))
                .super_agent_referral(configCacheMap.get(ConfigConstants.SUPER_AGENT_REFERRAL))
                .super_agent_subordinate(configCacheMap.get(ConfigConstants.SUPER_AGENT_SUBORDINATE))
                .super_agent_subordinate_referral(configCacheMap.get(ConfigConstants.SUPER_AGENT_SUBORDINATE_REFERRAL))
                .currency(vo).build();
        return paramConfig;
    }

    public void updateSpecialParam(SpecialParamConfigDTO paramConfig) {
        configMapper.replaceParam(ConfigConstants.BET_WARNING_AMOUNT, paramConfig.getBet_warning_amount());
        configMapper.replaceParam(ConfigConstants.DAILY_GIFT_AMOUNT, paramConfig.getDaily_gift_amount());
        configMapper.replaceParam(ConfigConstants.NEW_GIFT_AMOUNT, paramConfig.getNew_gift_amount());
        configMapper.replaceParam(ConfigConstants.KYC_AWARD,paramConfig.getKyc_award());
        List<CurrencyConfig> currency = paramConfig.getCurrency();
        if(CollectionUtils.isEmpty(currency)){
            return;
        }
        for (CurrencyConfig currencyConfig : currency) {
            currencyConfig.updateByName(followCurrencyMapper);
        }
    }

    public SpecialParamConfigVO getSpecialParam() {
        return SpecialParamConfigVO.build(configMapper, followCurrencyMapper);
    }

    @Resource
    BetDurationMapper betDurationMapper;

    @Resource
    ConfigMapper configMapper;

    @Resource
    FollowCurrencyMapper followCurrencyMapper;

}
