package com.tianli.management.spot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 * 现货充值提现表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("sg_charge")
public class SGCharge extends Model<SGCharge> {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime complete_time;
    private ChargeStatus status;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;
    private String uid_username;
    private String uid_nick;
    private String uid_avatar;
    private String sn;
    private String currency_type;
    private ChargeType charge_type;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal real_amount;
    private String from_address;
    private String to_address;
    private String txid;
    private String review_note;
    private BigDecimal miner_fee;
    private String miner_fee_type;
    private String token;
    private String reviewer;

    private Long reviewer_id;

    private LocalDateTime reviewer_time;

    private String reason;

    private String reason_en;
}
