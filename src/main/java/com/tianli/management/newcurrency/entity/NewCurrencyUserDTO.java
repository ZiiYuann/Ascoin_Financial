package com.tianli.management.newcurrency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 新币用户表
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@ApiModel(value="NewCurrencyUser对象", description="新币用户表")
public class NewCurrencyUserDTO extends Model<NewCurrencyUserDTO> {

    private static final long serialVersionUID=1L;
    //@ApiModelProperty(value = "币种名称")
    private String currencyName;
    //@ApiModelProperty(value = "开始时间")
    private String startTime;
    //@ApiModelProperty(value = "结束时间")
    private String endTime;

}
