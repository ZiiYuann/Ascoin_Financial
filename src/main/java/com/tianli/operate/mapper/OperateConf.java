package com.tianli.operate.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author linyifan
 *  2/25/21 4:31 PM
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class OperateConf {

    private List<OperateItem> location;

    private List<OperateItem> opType;

    @Data
    public static class OperateItem{
        String name;
        Integer type;
    }

}
