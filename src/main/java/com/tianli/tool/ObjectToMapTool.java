package com.tianli.tool;

import org.apache.commons.beanutils.BeanMap;

/**
 * @Author wangqiyun
 * @Date 2019/3/8 4:49 PM
 */
public class ObjectToMapTool {
    public static MapTool handle(Object o, String... fields) {
        if (o == null) return null;
        BeanMap beanMap = new BeanMap(o);
        MapTool result = MapTool.Map();
        for (String field : fields) {
            result.put(field, beanMap.get(field));
        }
        return result;
    }

}
