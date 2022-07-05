package com.tianli.tool.time;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.tianli.common.Constants;

/**
 * @Author wangqiyun
 * @Date 2019/6/28 19:02
 */
public class LocalDateTimeDes extends LocalDateTimeDeserializer {
    public LocalDateTimeDes() {
        super(Constants.standardDateTimeFormatter);
    }
}
