package com.tianli.mconfig.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author  wangqiyun
 * @since  2019-11-13 17:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private String name;
    private String value;
    /**
     * 描述
     */
    private String desc;
}
