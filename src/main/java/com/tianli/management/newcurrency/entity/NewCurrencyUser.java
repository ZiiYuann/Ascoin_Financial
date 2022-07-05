package com.tianli.management.newcurrency.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class NewCurrencyUser extends Model<NewCurrencyUser> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    //用户id
    private Long uid;

    //@ApiModelProperty(value = "币种")
    private String currency_name;
    //@ApiModelProperty(value = "币种配置表主键id")
    private Long currency_id;

    //@ApiModelProperty(value = "邮箱")
    private String email;

    //@ApiModelProperty(value = "客户类型")
    private Integer user_type;

    //@ApiModelProperty(value = "投入数额")
    private BigDecimal amount_buy;

    //@ApiModelProperty(value = "扣款数量")
    private BigDecimal amount_reduce;

    //@ApiModelProperty(value = "获得代币数量")
    private BigDecimal currency_count;

    //@ApiModelProperty(value = "投入时间")
    private LocalDateTime amount_buy_time;
    //新币创建时间
    @TableField(exist = false)
    private String new_currency_create_time;

//    @TableField(exist = false)
    private BigDecimal sale_price;//价格,字段冗余

//    @TableField(exist = false)
    private String currency_name_short;//币种简称

//    @TableField(exist = false)
    private String token;//代币种类,字段冗余

//    @TableField(exist = false)
    private String type;//订单状态

//    @TableField(exist = false)
    private BigDecimal amount_return;//返还数量

    @TableField(exist = false)
    private String timestamp_amount_buy;

    public void transferTime(){
        this.timestamp_amount_buy=toPochMilli(this.getAmount_buy_time());
    }

    public String toPochMilli(LocalDateTime localDateTime){
        if(localDateTime==null){
            return null;
        }
        ZoneId zone = ZoneId.systemDefault();
        long timestamp = localDateTime.atZone(zone).toInstant().toEpochMilli();
        return String.valueOf(timestamp);
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
