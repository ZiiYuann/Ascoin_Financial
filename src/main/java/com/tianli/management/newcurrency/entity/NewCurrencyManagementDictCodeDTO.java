package com.tianli.management.newcurrency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 新币管理端
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class NewCurrencyManagementDictCodeDTO extends Model<NewCurrencyManagementDictCodeDTO> {

    private static final long serialVersionUID=1L;

//    @TableId(value = "id", type = IdType.INPUT)
    private Long currencyId;
    //@ApiModelProperty(value = "币种名称")
    private String currencyName;


}
