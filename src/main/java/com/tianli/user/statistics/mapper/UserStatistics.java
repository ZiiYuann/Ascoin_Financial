package com.tianli.user.statistics.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * <p>
 * 用户统计表
 * </p>
 *
 * @author hd
 * @since 2020-12-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class UserStatistics {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 直邀人数
     */
    private Long referral_number;

    /**
     * 团队人数
     */
    private Long team_number;

    /**
     * 自身下单金额
     */
    private BigInteger my_amount;

    /**
     * 团队下单金额
     */
    private BigInteger team_amount;

    /**
     * 总返佣金额
     */
    private BigInteger rebate;

}
