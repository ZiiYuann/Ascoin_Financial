package com.tianli.management.ruleconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParamConfigVO {

    /**
     * 押注赢钱抽佣比例（普通场）
     */
    private String bet_rake_rate_normal;

    /**
     * 押注赢钱抽佣比例（稳赚场）
     */
    private String bet_rake_rate_steady;

    /**
     * 初级返佣比例
     */
    private String proportion_of_first_rebate;

    /**
     * 次级返佣比例
     */
    private String proportion_of_second_rebate;

    /**
     * 平台结算比例（普通场）
     */
    private String platform_rebate_rate_normal;

    /**
     * 平台结算比例（稳赚场）
     */
    private String platform_rebate_rate_steady;

    /**
     * 用户余额日利率
     */
    private String user_balance_daily_rate;

    /**
     * BF/USDT转化比例
     */
    private String BF_usdt_rate;

    /**
     * BF折扣优惠率
     */
    private String usdt_BF_discount_rate;

    /**
     * BF折扣门槛额
     */
    private String BF_switch_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（ERC20）
     */
    private String usdt_erc20_withdraw_rate;
    private String usdt_erc20_withdraw_fixed_amount;
    private String usdt_erc20_withdraw_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（OMNI）
     */
    private String usdt_omni_withdraw_rate;
    private String usdt_omni_withdraw_fixed_amount;
    private String usdt_omni_withdraw_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（bep20）
     */
    private String usdt_bep20_withdraw_rate;
    private String usdt_bep20_withdraw_fixed_amount;
    private String usdt_bep20_withdraw_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（BF）
     */
    private String BF_withdraw_rate;
    private String BF_withdraw_fixed_amount;
    private String BF_withdraw_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（TRC20）
     */
    private String usdt_trc20_withdraw_rate;
    private String usdt_trc20_withdraw_fixed_amount;
    private String usdt_trc20_withdraw_min_amount;

    /**
     * 代理商结算手续费比例, 手续费固定数额, 最低结算数额（ERC20）
     */
    private String usdt_erc20_agent_settle_rate;
    private String usdt_erc20_agent_settle_fixed_amount;
    private String usdt_erc20_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例, 手续费固定数额, 最低结算数额（OMNI）
     */
    private String usdt_omni_agent_settle_rate;
    private String usdt_omni_agent_settle_fixed_amount;
    private String usdt_omni_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例, 手续费固定数额, 最低结算数额（TRC20）
     */
    private String usdt_trc20_agent_settle_rate;
    private String usdt_trc20_agent_settle_fixed_amount;
    private String usdt_trc20_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例, 手续费固定数额, 最低结算数额（bep20）
     */
    private String usdt_bep20_agent_settle_rate;
    private String usdt_bep20_agent_settle_fixed_amount;
    private String usdt_bep20_agent_settle_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（usdc_bep20）
     */
    private String usdc_bep20_withdraw_rate;
    private String usdc_bep20_withdraw_fixed_amount;
    private String usdc_bep20_withdraw_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（usdc_ERC20）
     */
    private String usdc_erc20_withdraw_rate;
    private String usdc_erc20_withdraw_fixed_amount;
    private String usdc_erc20_withdraw_min_amount;

    /**
     * 用户提现手续费比例, 手续费固定数额, 最低提现数额（usdc_TRC20）
     */
    private String usdc_trc20_withdraw_rate;
    private String usdc_trc20_withdraw_fixed_amount;
    private String usdc_trc20_withdraw_min_amount;

    /**
     * 代理商结算手续费比例, 手续费固定数额, 最低结算数额（BF）
     */
    private String BF_agent_settle_rate;
    private String BF_agent_settle_fixed_amount;
    private String BF_agent_settle_min_amount;

    /**
     * 待归记录手续费比例
     */
    private String record_to_be_collected_rate;

    /**
     * 币种设置
     */
    private List<CurrencyConfig> currency;

    /**
     * 需KYC提现门槛值
     */
    private String kyc_trigger_amount;
    /**
     * 用户现货手续费
     */
    private String actual_buy_rate;
    private String actual_sell_rate;
    private String actual_withdraw_rate;

}
