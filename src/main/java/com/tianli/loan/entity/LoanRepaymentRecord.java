package com.tianli.loan.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 贷款-还款记录表
 * </p>
 *
 * @author lzy
 * @since 2022-06-06
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("loan_repayment_record")
public class LoanRepaymentRecord extends Model<LoanRepaymentRecord> {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long uid;

    private Long loan_id;

    /**
     * 还款金额
     */
    private BigDecimal amount;

    /**
     * 转出地址
     */
    @TableField("`from`")
    private String from;

    /**
     * 转入地址
     */
    @TableField("`to`")
    private String to;

    /**
     * 哈希
     */
    private String hash;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    /**
     * 交易时间
     */
    private String tr_time;

    /**
     * 币类型
     */
    private String token;

    /**
     * 链类型
     */
    private String chain_type;

    /**
     * 偿还本金
     */
    private BigDecimal paid_principal;

    /**
     * 偿还滞纳金
     */
    private BigDecimal paid_forfeit_penalty;

    /**
     * 偿还利息
     */
    private BigDecimal paid_interest;
    /**
     * 剩余本金
     */
    private BigDecimal remaining_principal;
    /**
     * 剩余滞纳金
     */
    private BigDecimal remaining_forfeit_penalty;
    /**
     * 剩余利息
     */
    private BigDecimal remaining_interest;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
