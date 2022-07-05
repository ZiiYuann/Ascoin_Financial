package com.tianli.agent.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.user.mapper.UserIdentity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 代理商表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Agent extends Model<Agent> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 代理商类型,一级代理商or低级代理商
     */
    private UserIdentity identity;

    /**
     * 上级代理商id,一级代理商为0
     */
    private Long senior_id;

    /**
     * 代理商名称
     */
    private String nick;

    /**
     * 代理商手机号
     */
    private String username;

    /**
     * 期望押金
     */
    private BigInteger expect_deposit;

    /**
     * 代理商总净盈亏
     */
    private BigInteger profit;

    /**
     * 已结算数额
     */
    private BigInteger settled_number;

    /**
     * 商定分红
     */
    private Double expect_dividends;

    /**
     * 实际分红
     */
    private Double real_dividends;

    /**
     * 商定分红
     */
    private Double steady_dividends;

    /**
     * 普通场返佣比例
     */
    private Double normal_rebate_proportion;

    /**
     * 稳赚场返佣比例
     */
    private Double steady_rebate_proportion;

    /**
     * 是否需要关注
     */
    private Boolean focus;

    /**
     * 备注
     */
    private String note;

    /**
     * 是否是超级代理商
     */
    private Boolean super_agent;

    /**
     * 给直邀设置的普通场返佣比例
     */
    private Double invite_normal_rebate_proportion;

    /**
     * 给直邀设置的稳赚场返佣比例
     */
    private Double invite_steady_rebate_proportion;

}
