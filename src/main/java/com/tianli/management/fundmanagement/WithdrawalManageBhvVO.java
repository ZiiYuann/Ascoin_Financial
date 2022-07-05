package com.tianli.management.fundmanagement;

import com.tianli.charge.mapper.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalManageBhvVO {
    private Long id;
    private String uid_username;
    private Long uid;
    private double amount;
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

    public static WithdrawalManageBhvVO trans(WithdrawalManageBhvPO po) {
        WithdrawalManageBhvVO withdrawalManageBhvVO = new WithdrawalManageBhvVO();
        BeanUtils.copyProperties(po, withdrawalManageBhvVO);
        withdrawalManageBhvVO.setAmount(po.getCurrency_type().money(po.getAmount()));
        return withdrawalManageBhvVO;
    }
}
