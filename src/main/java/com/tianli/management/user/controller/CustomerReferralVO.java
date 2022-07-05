package com.tianli.management.user.controller;

import com.tianli.user.mapper.User;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户推荐表
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CustomerReferralVO {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 推荐时间
     */
    private LocalDateTime create_time;

    /**
     * 推荐用户id
     */
    private Long referral_id;

    /**
     * 邀请人手机号
     */
    private String referral_username;

    /**
     * 人机校验分数
     */
    private double grc_score;


    public static CustomerReferralVO trans(User user, Long id) {
        return CustomerReferralVO.builder()
                .id(id)
                .create_time(user.getCreate_time())
                .referral_id(user.getId())
                .referral_username(user.getUsername())
                .build();
    }
}
