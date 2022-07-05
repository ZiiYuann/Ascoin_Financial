package com.tianli.loan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户还款地址表
 * </p>
 *
 * @author lzy
 * @since 2022-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("loan_address")
public class LoanAddress extends Model<LoanAddress> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 比特币地址btc / usdt-omni
     */
    private String btc;

    /**
     * 以太坊地址eth / usdt-erc20
     */
    private String eth;

    /**
     * 波场地址 tron / usdt-trc20
     */
    private String tron;

    private String bsc;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public LoanAddress() {
    }

    public LoanAddress(Long id, Long uid, String btc, String eth, String tron, String bsc) {
        this.id = id;
        this.uid = uid;
        this.create_time = LocalDateTime.now();
        this.btc = btc;
        this.eth = eth;
        this.tron = tron;
        this.bsc = bsc;
    }

}
