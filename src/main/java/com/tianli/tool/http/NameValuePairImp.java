package com.tianli.tool.http;

import com.tianli.exception.ErrCodeException;
import org.springframework.util.StringUtils;

/**
 * Created by wangqiyun on 2017/6/3.
 */
public class NameValuePairImp implements org.apache.http.NameValuePair {
    private String name;
    private String value;

    public NameValuePairImp(String name, String value) {
        this.name = name;
        this.value = value;
        if (StringUtils.isEmpty(name) || value == null)
            throw new ErrCodeException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }
}
