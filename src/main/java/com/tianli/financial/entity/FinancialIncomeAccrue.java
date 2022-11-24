package com.tianli.financial.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialIncomeAccrue extends Model<FinancialIncomeAccrue> {

    @Id
    private String id;

    private Long uid;

    /**
     * 申购记录id
     */
    private Long recordId;

    private String coin;

    /**
     * 累计收益金额
     */
    private BigDecimal incomeAmount;

    /**
     * 最近更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
