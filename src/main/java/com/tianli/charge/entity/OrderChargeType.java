package com.tianli.charge.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String type;

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
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String operationType;

    /**
     * 操作类型组名
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String operationGroup;

    /**
     * 可见类型 0:代理可见,1用户可见
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long visibleType;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}