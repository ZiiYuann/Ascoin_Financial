package com.tianli.management.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 云钱包代理人和产品关联
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class WalletAgentProduct extends Model<WalletAgentProduct> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 代理ID
     */
    private Long agentId;

    /**
     * 用户ID
     */
    private Long uid;
    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 推荐码
     */
    private String referralCode;


}
