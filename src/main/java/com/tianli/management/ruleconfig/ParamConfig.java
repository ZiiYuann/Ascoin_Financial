package com.tianli.management.ruleconfig;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @author chensong
 * @date 2021-02-19 10:19
 * @since 1.0.0
 */
@Builder
@Data
public class ParamConfig {

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
     * 普通会员反佣比例（普通场)
     */
    @NotEmpty(message = "请输入普通会员返佣比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "普通会员反佣比例大于0小于100，小数点后最多两位小数")
    private String user_rebate_rate_normal;

    /**
     * 普通会员反佣比例（稳赚场)
     */
    @NotEmpty(message = "请输入普通会员返佣比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "普通会员反佣比例大于0小于100，小数点后最多两位小数")
    private String user_rebate_rate_steady;

    /**
     * 用户余额日利率
     */
    @NotEmpty(message = "请输入用户余额日利率")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "用户余额日利率大于0小于100，小数点后最多两位小数")
    private String user_balance_daily_rate;

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
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须大于0且是整数")
    private String usdt_erc20_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（ERC20）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须大于0且是整数")
    private String usdt_erc20_withdraw_min_amount;

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
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户提现手续费固定数额必须大于0且是整数")
    private String usdt_omni_withdraw_fixed_amount;

    /**
     * 用户最低提现数额（OMNI）
     */
    @NotEmpty(message = "请输入用户最低提现数额")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "用户最低提现数额必须大于0且是整数")
    private String usdt_omni_withdraw_min_amount;

    /**
     * 平台抽水比例
     */
    @NotEmpty(message = "请输入平台抽水比例（普通场）")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "平台抽水比例（普通场）大于0小于100，小数点后最多两位小数")
    private String platform_rebate_rate_normal;

    /**
     * 平台抽水比例
     */
    @NotEmpty(message = "请输入平台抽水比例（稳赚场）")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "平台抽水比例（稳赚场）大于0小于100，小数点后最多两位小数")
    private String platform_rebate_rate_steady;

    /**
     * 超级代理商抽水比例
     */
    @NotEmpty(message = "请输入代理商抽水比例")
    @Pattern(regexp = ConfigConstants.PERCENTAGE_REGEX,message = "代理商抽水比例大于0小于100，小数点后最多两位小数")
    private String super_agent_rake_rate;

    /**
     * 超级代理商直邀人数
     */
    @NotEmpty(message = "请输入超级代理商直邀人数")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "超级代理商直邀人数必须大于0且是整数")
    private String super_agent_referral;

    /**
     * 超级代理商下级代理商
     */
    @NotEmpty(message = "请输入超级代理商邀请人数门槛")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "超级代理商直邀人数门槛必须大于0且是整数")
    private String super_agent_subordinate;

    /**
     * 超级代理商下级代理商
     */
    @NotEmpty(message = "请输入超级代理商下级代理直邀人数")
    @Pattern(regexp = ConfigConstants.POSITIVE_INTEGER_REGEX,message = "超级代理商下级代理直邀人数必须大于0且是整数")
    private String super_agent_subordinate_referral;

    private List<CurrencyConfig> currency;

}
