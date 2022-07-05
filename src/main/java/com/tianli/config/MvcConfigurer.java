package com.tianli.config;

import com.tianli.common.uid.UidNotNullResolver;
import com.tianli.common.uid.UidResolver;
import com.tianli.tool.JacksonUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2020/7/16 17:39
 */

@Configuration
public class MvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UidResolver());
        resolvers.add(new UidNotNullResolver());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.forEach(JacksonUtils.wrapperObjectMapper());
    }
}
