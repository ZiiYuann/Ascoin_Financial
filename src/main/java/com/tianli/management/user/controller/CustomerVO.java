package com.tianli.management.user.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.user.mapper.CustomerDTO;
import com.tianli.user.mapper.UserStatus;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
public class CustomerVO {
    private Long id;
    private String phone;
    private String nick;
    private UserStatus status;
    private LocalDateTime create_time;
    /**
     * eth / usdt-erc20
     */
    private String eth;
    /**
     * btc / usdt-omni
     */
    private String btc;

    /**
     * trc20
     */
    private String trc20;

    /**
     * trc20
     */
    private String bsc;

    private double balance;
    private double balance_BF;
    /**
     * 优惠余额
     */
    private double weak_balance;

    private String facebook;

    private String line;

    private Boolean use_robot;
//
//    private Integer auto_count;
//
//    private BigDecimal auto_amount;
//
//    /**
//     * 间隔时间
//     */
//    private String interval_time;
//
//    /**
//     * 胜率
//     */
//    private Double win_rate;
//
//    /**
//     * 利润率
//     */
//    private Double profit_rate;

    /**
     * 备注
     */
    private String node;
    private Integer credit_score;
    private String adjust_reason;
    private Integer user_type;

    private String salesman_username;

    /**
     * 充值金额
     */
    private BigDecimal recharge_amount;
    /**
     * 提现金额
     */
    private BigDecimal withdrawal_amount;
    /**
     * 利润
     */
    private BigDecimal profit;

    public static CustomerVO trans(CustomerDTO dto) {
        return CustomerVO.builder()
                .id(dto.getId())
                .phone(dto.getPhone())
                .nick(dto.getNick())
                .status(dto.getStatus())
                .create_time(dto.getCreate_time())
                .eth(dto.getEth())
                .btc(dto.getBtc())
                .trc20(dto.getTrc20())
                .bsc(dto.getBsc())
                .use_robot(dto.getUse_robot())
                .salesman_username(dto.getSalesman_username())
                .recharge_amount(dto.getRecharge_amount())
                .withdrawal_amount(dto.getWithdrawal_amount())
                .profit(dto.getProfit())
//                .auto_count(dto.getAuto_count())
//                .auto_amount(dto.getAuto_amount())
//                .interval_time(dto.getInterval_time())
//                .win_rate(dto.getWin_rate())
//                .profit_rate(dto.getProfit_rate())
                .node(dto.getNode())
                .credit_score(dto.getCredit_score())
                .adjust_reason(dto.getAdjust_reason())
                .user_type(dto.getUser_type())
                .balance_BF(Objects.isNull(dto.getBalance()) ? 0 : CurrencyTokenEnum.BF_bep20.money(dto.getBalance_BF()))
                .balance(Objects.isNull(dto.getBalance()) ? 0 : TokenCurrencyType.usdt_omni.money(dto.getBalance()))
                .weak_balance(Objects.isNull(dto.getWeak_balance()) ? 0 : TokenCurrencyType.usdt_omni.money(dto.getWeak_balance()))
                .facebook(StringUtils.isBlank(dto.getFacebook()) ? "未绑定" : dto.getFacebook())
                .line(StringUtils.isBlank(dto.getLine()) ? "未绑定" : dto.getLine())
                .build();
    }
}
