package com.tianli.address.mapper;

import com.tianli.account.enums.ProductType;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户充值地址表
 * </p>
 *
 * @author hd
 * @since 2020-12-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 比特币地址btc / usdt-omni
     */
    private String btc;

    /**
     * 以太坊地址eth / usdt-erc20
     */
    private String eth;

    /**
     * 波场地址 tron
     */
    private String tron;

    /**
     * BSC地址 tron
     */
    private String bsc;

}
