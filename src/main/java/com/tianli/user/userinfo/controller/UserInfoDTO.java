package com.tianli.user.userinfo.controller;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 *  用户信息表
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class UserInfoDTO {

    /**
     * 头像地址
     */
    @NotBlank(message = "头像不能为空")
    private String avatar;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nick;

}
