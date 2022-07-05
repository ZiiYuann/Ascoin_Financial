package com.tianli.currency.mapper;

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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class ArtificialRecharge extends Model<ArtificialRecharge> {
   private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    private ArtificialRechargeType type;
    /**
     * 充值log表主键id
     */
    private String log_id;
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
