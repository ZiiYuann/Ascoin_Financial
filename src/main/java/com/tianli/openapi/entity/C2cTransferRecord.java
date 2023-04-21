package com.tianli.openapi.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.tianli.charge.enums.ChargeType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author xianeng
 * @since 2023-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class C2cTransferRecord extends Model<C2cTransferRecord> {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * uid
     */
    private Long uid;

    /**
     * 金额
     */
    private BigDecimal amount;


    private ChargeType chargeType;

    /**
     * 币种
     */
    private String coin;

    /**
     * c2c订单id
     */
    private String c2cOrderNo;

    /**
     * 外键
     */
    private Long externalPk;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
