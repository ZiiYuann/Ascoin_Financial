package com.tianli.loan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 贷款的log
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("loan_currency_log")
public class LoanCurrencyLog extends Model<LoanCurrencyLog> {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 创建时间
     */
    private Long create_time_ms;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * log类型
     */
    private String type;

    /**
     * 代币类型
     */
    private String token;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 最终金额
     */
    private BigDecimal to_balance;

    /**
     * 关联的id
下注, 贷款等
     */
    private Long relate_id;

    /**
     * 备注
     */
    private String node;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
