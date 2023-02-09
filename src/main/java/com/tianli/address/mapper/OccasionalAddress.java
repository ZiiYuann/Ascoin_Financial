package com.tianli.address.mapper;

import com.tianli.chain.enums.ChainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 作为address表的延伸 采用懒加载的方式保存一些不常用的链的充值地址
 * addressMnemonic 保存addressId对应的助记词 用于生成非evm的充值地址保存至本表
 * @Author cs
 * @Date 2022-12-26 17:24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccasionalAddress {
    private Long id;
    private Long addressId;
    private ChainType chain;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean registered;
    private Integer retryCount;
}
