package com.tianli.management.ruleconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParamConfigDTO {

    /**
     * 押注赢钱抽佣比例（普通场）
     */
    @NotEmpty(message = "请输入押注赢钱抽佣比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "押注赢钱抽佣比例大于0小于100，小数点后最多两位小数")
    private String bet_rake_rate_normal;

    /**
     * 押注赢钱抽佣比例（稳赚场）
     */
    @NotEmpty(message = "请输入押注赢钱抽佣比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "押注赢钱抽佣比例大于0小于100，小数点后最多两位小数")
    private String bet_rake_rate_steady;

    /**
     * 初级反佣比例
     */
    @NotEmpty(message = "请输入初级反佣比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "初级反佣比例大于0小于100，小数点后最多两位小数")
    private String proportion_of_first_rebate;

    /**
     * 次级反佣比例
     */
    @NotEmpty(message = "请输入次级反佣比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "次级反佣比例大于0小于100，小数点后最多两位小数")
    private String proportion_of_second_rebate;

    /**
     * 平台结算比例（普通场）
     */
    @NotEmpty(message = "请输入平台结算比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "平台结算比例大于0小于100，小数点后最多两位小数")
    private String platform_rebate_rate_normal;

    /**
     * 平台结算比例（稳赚场）
     */
    @NotEmpty(message = "请输入平台结算比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "平台结算比例大于0小于100，小数点后最多两位小数")
    private String platform_rebate_rate_steady;

    /**
     * 用户余额日利率
     */
//    @NotEmpty(message = "请输入用户余额日利率")
//    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户余额日利率大于0小于100，小数点后最多两位小数")
//    private String user_balance_daily_rate;

    /**
     * BF/USDT转化比例
     */
    @NotEmpty(message = "请输入BF/USDT转化比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "BF/USDT转化比例大于0小于100，小数点后最多两位小数")
    private String BF_usdt_rate;

    /**
     * BF/USDT转化比例
     */
    @NotEmpty(message = "请输入BF折扣率优惠率")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "BF折扣率优惠率大于0小于100，小数点后最多两位小数")
    private String usdt_BF_discount_rate;

    /**
     * BF折扣门槛额
     */
    @NotEmpty(message = "请输入BF/USDT转化比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "BF/USDT转化比例大于0小于100，小数点后最多两位小数")
    private String BF_switch_min_amount;

    /**
     * 用户提现手续费比例（ERC20）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_erc20_withdraw_rate;

    /**
     * 用户提现手续费固定数额（ERC20）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdt_erc20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（ERC20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdt_erc20_withdraw_min_amount;
    /**
     * 用户提现手续费比例（BEP20）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_bep20_withdraw_rate;

    /**
     * 用户提现手续费固定数额（BEP20）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdt_bep20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（BEP20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdt_bep20_withdraw_min_amount;

    /**
     * 用户提现手续费比例（OMNI）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_omni_withdraw_rate;

    /**
     * 用户提现手续费固定数额（OMNI）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdt_omni_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（OMNI）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdt_omni_withdraw_min_amount;

    /**
     * 用户提现手续费比例（TRC20）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_trc20_withdraw_rate;

    /**
     * 用户提现手续费固定数额（TRC20）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdt_trc20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（TRC20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdt_trc20_withdraw_min_amount;

    /**
     * 用户提现手续费比例（USDC_ERC20）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdc_erc20_withdraw_rate;

    /**
     * 用户提现手续费固定数额（USDC_ERC20）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdc_erc20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（USDC_ERC20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdc_erc20_withdraw_min_amount;
    /**
     * 用户提现手续费比例（USDC_BEP20）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdc_bep20_withdraw_rate;

    /**
     * 用户提现手续费固定数额（USDC_BEP20）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdc_bep20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（USDC_BEP20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdc_bep20_withdraw_min_amount;

    /**
     * 用户提现手续费比例（USDC_TRC20）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String usdc_trc20_withdraw_rate;

    /**
     * 用户提现手续费固定数额（USDC_TRC20）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String usdc_trc20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（USDC_TRC20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String usdc_trc20_withdraw_min_amount;

    /**
     * 用户提现手续费比例（BF）
     */
    @NotEmpty(message = "请输入用户提现手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户提现手续费比例大于0小于100，小数点后最多两位小数")
    private String BF_withdraw_rate;

    /**
     * 用户提现手续费固定数额（BF）
     */
    @NotEmpty(message = "请输入用户提现手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须是整数")
    private String BF_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（BF）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须是整数")
    private String BF_withdraw_min_amount;

    /**
     * 代理商结算手续费比例（ERC20）
     */
    @NotEmpty(message = "请输入代理商结算手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商结算手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_erc20_agent_settle_rate;

    /**
     * 代理商结算手续费固定数额（ERC20）
     */
    @NotEmpty(message = "请输入代理商结算手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商结算手续费固定数额必须是整数")
    private String usdt_erc20_agent_settle_fixed_amount;

    /**
     * 代理商最低结算数额（ERC20）
     */
    @NotEmpty(message = "请输入代理商最低结算数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商最低结算额必须是整数")
    private String usdt_erc20_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例（BEP20）
     */
    @NotEmpty(message = "请输入代理商结算手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商结算手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_bep20_agent_settle_rate;

    /**
     * 代理商结算手续费固定数额（BEP20）
     */
    @NotEmpty(message = "请输入代理商结算手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商结算手续费固定数额必须是整数")
    private String usdt_bep20_agent_settle_fixed_amount;

    /**
     * 代理商最低结算数额（BEP20）
     */
    @NotEmpty(message = "请输入代理商最低结算数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商最低结算额必须是整数")
    private String usdt_bep20_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例（OMNI）
     */
    @NotEmpty(message = "请输入代理商结算手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商结算手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_omni_agent_settle_rate;

    /**
     * 代理商结算手续费固定数额（OMNI）
     */
    @NotEmpty(message = "请输入代理商结算手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商结算手续费固定数额必须是整数")
    private String usdt_omni_agent_settle_fixed_amount;

    /**
     * 代理商最低结算数额（OMNI）
     */
    @NotEmpty(message = "请输入代理商最低结算数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商最低结算额必须是整数")
    private String usdt_omni_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例（TRC20）
     */
    @NotEmpty(message = "请输入代理商结算手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商结算手续费比例大于0小于100，小数点后最多两位小数")
    private String usdt_trc20_agent_settle_rate;

    /**
     * 代理商结算手续费固定数额（TRC20）
     */
    @NotEmpty(message = "请输入代理商结算手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商结算手续费固定数额必须是整数")
    private String usdt_trc20_agent_settle_fixed_amount;

    /**
     * 代理商最低结算数额（TRC20）
     */
    @NotEmpty(message = "请输入代理商最低结算数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商最低结算额必须是整数")
    private String usdt_trc20_agent_settle_min_amount;

    /**
     * 代理商结算手续费比例（BF）
     */
    @NotEmpty(message = "请输入代理商结算手续费比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商结算手续费比例大于0小于100，小数点后最多两位小数")
    private String BF_agent_settle_rate;

    /**
     * 代理商结算手续费固定数额（BF）
     */
    @NotEmpty(message = "请输入代理商结算手续费固定数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商结算手续费固定数额必须是整数")
    private String BF_agent_settle_fixed_amount;

    /**
     * 代理商最低结算数额（BF）
     */
    @NotEmpty(message = "请输入代理商最低结算数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商最低结算额必须是整数")
    private String BF_agent_settle_min_amount;

//    /**
//     * 代理商撤回保证金手续费比例（ERC20）
//     */
//    @NotEmpty(message = "请输入代理商撤回保证金手续费比例")
//    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商撤回保证金手续费比例大于0小于100，小数点后最多两位小数")
//    private String usdt_erc20_agent_withdraw_rate;
//
//    /**
//     * 代理商撤回保证金手续费固定数额（ERC20）
//     */
//    @NotEmpty(message = "请输入代理商撤回保证金手续费固定数额")
//    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商撤回保证金手续费固定数额必须是整数")
//    private String usdt_erc20_agent_withdraw_fixed_amount;
//
//    /**
//     * 代理商保证金最低撤回数额（ERC20）
//     */
//    @NotEmpty(message = "请输入代理商保证金最低撤回数额")
//    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商保证金最低撤回数额必须是整数")
//    private String usdt_erc20_agent_withdraw_min_amount;
//
//    /**
//     * 代理商撤回保证金手续费比例（OMNI）
//     */
//    @NotEmpty(message = "请输入代理商撤回保证金手续费比例")
//    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商撤回保证金手续费比例大于0小于100，小数点后最多两位小数")
//    private String usdt_omni_agent_withdraw_rate;
//
//    /**
//     * 代理商撤回保证金手续费固定数额（OMNI）
//     */
//    @NotEmpty(message = "请输入代理商撤回保证金手续费固定数额")
//    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商撤回保证金手续费固定数额必须是整数")
//    private String usdt_omni_agent_withdraw_fixed_amount;
//
//    /**
//     * 代理商保证金最低撤回数额（OMNI）
//     */
//    @NotEmpty(message = "请输入代理商保证金最低撤回数额")
//    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商保证金最低撤回数额必须是整数")
//    private String usdt_omni_agent_withdraw_min_amount;
//
//    /**
//     * 代理商撤回保证金手续费比例（TRC20）
//     */
//    @NotEmpty(message = "请输入代理商撤回保证金手续费比例")
//    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商撤回保证金手续费比例大于0小于100，小数点后最多两位小数")
//    private String usdt_trc20_agent_withdraw_rate;
//
//    /**
//     * 代理商撤回保证金手续费固定数额（TRC20）
//     */
//    @NotEmpty(message = "请输入代理商撤回保证金手续费固定数额")
//    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商撤回保证金手续费固定数额必须是整数")
//    private String usdt_trc20_agent_withdraw_fixed_amount;
//
//    /**
//     * 代理商保证金最低撤回数额（TRC20）
//     */
//    @NotEmpty(message = "请输入代理商保证金最低撤回数额")
//    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "代理商保证金最低撤回数额必须是整数")
//    private String usdt_trc20_agent_withdraw_min_amount;

    /**
     * 待归记录手续费比例
     */
//    @NotEmpty(message = "请输入待归集记录手续费比例")
//    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代待归集记录手续费比例大于0小于100，小数点后最多两位小数")
    private String record_to_be_collected_rate;

    @NotBlank(message = "请输入KYC提现门槛值")
    private String kyc_trigger_amount;

    @NotBlank(message = "请输入现货交易买入手续费")
    private String actual_buy_rate;

    @NotBlank(message = "请输入现货交易卖出手续费")
    private String actual_sell_rate;

    @NotBlank(message = "请输入现货交易提现手续费")
    private String actual_withdraw_rate;

    private List<CurrencyConfig> currency;

}
