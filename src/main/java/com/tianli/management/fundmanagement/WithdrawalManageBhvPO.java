package com.tianli.management.fundmanagement;

import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalManageBhvPO {
    private Long id;
    private String uid_username;
    private Long uid;
    private BigInteger amount;
    private TokenCurrencyType currency_type;
    private String to_address;
    private ChargeStatus status;
    private LocalDateTime create_time;
    /**
     * 谷歌校验分数
     */
    private Double grc_score;
    private Boolean grc_result;
    /**
     * 设备信息
     */
    private String ip;
    private String equipment_type;
    private String equipment;

    /**
     * 国家
     */
    private String country;

    /**
     * 地区
     */
    private String region;

    /**
     * 城市
     */
    private String city;

    /**
     * 上级代理
     */
    private String referral_username;

    //登录校验成功/失败次数：5/10
    private int login_success;
    private int login_fail;
    //押注成功/失败校验次数：5/10
    private int bet_success;
    private int bet_fail;
    //提现成功/失败校验次数：5/10
    private int withdrawal_success;
    private int withdrawal_fail;
    //同ip不同账号提现次数：3
    private int withdrawal_same_ip;
    //同设备号不同账号提现次数：3
    private int withdrawal_same_equipment;

    public void fillProperties(Map<String, BigDecimal> map, Map<String, Long> map2, Map<String, Long> map3) {
        if (!CollectionUtils.isEmpty(map)) {
            this.setLogin_success(map.getOrDefault("login_success", BigDecimal.ZERO).intValue());
            this.setLogin_fail(map.getOrDefault("login_fail", BigDecimal.ZERO).intValue());

            this.setBet_success(map.getOrDefault("bet_success", BigDecimal.ZERO).intValue());
            this.setBet_fail(map.getOrDefault("bet_fail", BigDecimal.ZERO).intValue());

            this.setWithdrawal_success(map.getOrDefault("withdrawal_success", BigDecimal.ZERO).intValue());
            this.setWithdrawal_fail(map.getOrDefault("withdrawal_fail", BigDecimal.ZERO).intValue());
        }

        if (!CollectionUtils.isEmpty(map2)) {
            this.setWithdrawal_same_ip(map2.getOrDefault("withdrawal_same_ip", 1L).intValue());
        }
        if (!CollectionUtils.isEmpty(map3)) {
            this.setWithdrawal_same_equipment(map3.getOrDefault("withdrawal_same_equipment", 1L).intValue());

        }
    }
}
