package com.tianli.address.mapper;

import com.tianli.chain.enums.ChainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * eth bsc tron等常用链的充值地址初始化保存到address表中
 * 除此之外的表采用懒加载，仅在用户获取充值地址的时候生成并保存的当前表
 * @Author cs
 * @Date 2022-12-26 17:24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccasionalAddress {
    private Long id;
    private Long uid;
    private ChainType chain;
    private String address;
    private LocalDateTime createTime;
}
