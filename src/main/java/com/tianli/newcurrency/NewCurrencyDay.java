package com.tianli.newcurrency;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 每天0点用户币余额
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@ApiModel(value="NewCurrencyDay对象", description="每天0点用户币余额")
public class NewCurrencyDay extends Model<NewCurrencyDay> {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long uid;
    /**
     * 余额类型
     */
    private String type;

    /**
     * 余额类型
     */
    private String token;

    /**
     * 总余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal freeze;

    /**
     * 剩余余额
     */
    private BigDecimal remain;


    private LocalDateTime create_time;
}
