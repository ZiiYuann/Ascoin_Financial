package com.tianli.account.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 用户余额汇总表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance extends Model<AccountBalance> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * logo 地址  可以删除
     */
    private String logo;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 余额类型
     */
    private String coin;

    /**
     * 总余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal freeze;

    /**
     * 剩余余额
     */
    private BigDecimal remain;

}
