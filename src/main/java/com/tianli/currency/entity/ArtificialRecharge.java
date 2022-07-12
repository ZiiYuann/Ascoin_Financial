package com.tianli.currency.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 人工充值表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ArtificialRecharge extends Model<ArtificialRecharge> {
   private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 充值log表主键id
     */
    private String logId;
    private Long uid;
    private String username;
    private String nick;
    private String avatar;
    private BigInteger amount;
    /**
     * 凭证图片["https//www.baidu.com/123.png"]
     */
    private String voucher_image;
    /**
     * 是否撤销: 0未撤销, 1撤销
     */
    private Boolean revoked;
    /**
     * 操作管理员
     */
    private Long recharge_admin_id;
    private String recharge_admin_nick;
    private String remark;

    /**
     * 撤回管理员
     */
    private Long revoke_admin_id;
    private String revoke_admin_nick;
    
}
