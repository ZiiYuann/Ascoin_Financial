package com.tianli.tool.time;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.tianli.common.Constants;

/**
 * @Author wangqiyun
 * @Date 2019/6/28 19:01
 */
public class LocalDateTimeSer extends LocalDateTimeSerializer {
    public LocalDateTimeSer() {
        super(Constants.standardDateTimeFormatter);
    }
}
