package com.tianli.common.uid;

import com.tianli.common.init.RequestInitService;
import com.tianli.tool.ApplicationContextTool;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @Author wangqiyun
 * @Date 2020/7/16 17:52
 */
public class UidNotNullResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().isAssignableFrom(long.class) && methodParameter.hasParameterAnnotation(Uid.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return ApplicationContextTool.getBean(RequestInitService.class).uid();
    }
}
