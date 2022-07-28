package com.tianli.borrow.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 质押币种配置
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BorrowPledgeCoinConfig extends Model<BorrowPledgeCoinConfig> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    private Long id;

    /**
     * 币种
     */
    private String coin;

    /**
     * 初始质押率
     */
    private BigDecimal initialPledgeRate;

    /**
     * 警告质押率
     */
    private BigDecimal warnPledgeRate;

    /**
     * 强平质押率
     */
    private BigDecimal liquidationPledgeRate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 删除标识
     */
    private Integer isDel;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
