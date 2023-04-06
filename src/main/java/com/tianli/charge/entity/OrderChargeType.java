package com.tianli.charge.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author xianeng
 * @since 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderChargeType extends Model<OrderChargeType> {

    private static final long serialVersionUID=1L;

    /**
     * 自增主键 不返回该字段
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer id;

    /**
     * 交易类型
     */
    private ChargeType type;

    /**
     * 交易名称
     */
    private String name;

    /**
     * 国际化名称
     */
    @TableField("nameEn")
    private String nameEn;

    /**
     * 操作分类(暂时无用)
     */
    private OperationTypeEnum operationType;

    /**
     * 操作类型组名
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ChargeTypeGroupEnum operationGroup;

    /**
     * 可见类型 0:代理可见,1用户可见
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long visibleType;

    /**
     * 是否启用 1:是；0否
     */
    private Integer isEnable;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
