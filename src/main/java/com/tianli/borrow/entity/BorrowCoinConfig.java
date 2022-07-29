package com.tianli.borrow.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 借币数据配置
 * </p>
 *
 * @author xn
 * @since 2022-07-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BorrowCoinConfig extends Model<BorrowCoinConfig> {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 币种
     */
    private String coin;

    /**
     * 最小可借
     */
    private BigDecimal minimumBorrow;

    /**
     * 最大可借
     */
    private BigDecimal maximumBorrow;

    /**
     * 年利率
     */
    private BigDecimal annualInterestRate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 删除标记
     */
    private Integer isDel;

}
