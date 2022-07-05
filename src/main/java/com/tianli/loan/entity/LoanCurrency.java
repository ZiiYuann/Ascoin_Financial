package com.tianli.loan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户贷款余额表
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
@TableName("loan_currency")
public class LoanCurrency extends Model<LoanCurrency> {

    private static final long serialVersionUID=1L;

    private Long id;

    private Long uid;

    /**
     * 币类型
     */
    private String token;

    /**
     * 余额
     */
    private BigDecimal balance;

    private LocalDateTime create_time;

    private Long create_time_ms;

    private LocalDateTime update_time;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
