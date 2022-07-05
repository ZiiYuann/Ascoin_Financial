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
 * @Date 2020/7/16 17:30
 */
public class UidResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().isAssignableFrom(Long.class) && methodParameter.hasParameterAnnotation(Uid.class);
    }

    @Override
    public Long resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return ApplicationContextTool.getBean(RequestInitService.class)._uid();
    }

}
