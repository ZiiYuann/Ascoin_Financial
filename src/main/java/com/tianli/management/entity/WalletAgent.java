package com.tianli.management.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 云钱包代理人
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WalletAgent extends Model<WalletAgent> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 代理人ID
     */
    private Long uid;

    /**
     * 代理人名称
     */
    private String agentName;

    /**
     * 登陆密码
     */
    private String loginPassword;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 删除标记
     */
    private Boolean deleted;


}
